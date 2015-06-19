package com.mycode.myreactivecamel;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;

public class ReactiveSource extends RouteBuilder {

    SourceKind kind;
    String notateEndpoint;
    String consumeEndpoint;
    Map<SourceKind, Boolean> arrivedNotation
            = Collections.synchronizedMap(new EnumMap<SourceKind, Boolean>(SourceKind.class));

    public ReactiveSource(SourceKind k) {
        this.kind = k;
    }

    @Override
    public void configure() throws Exception {
        from(notateEndpoint)
                .throttle(1).filter().method(this, "checkReadyToConsume").to(consumeEndpoint);

        from(consumeEndpoint)
                .throttle(1).filter().method(this, "checkShouldCompute").bean(this, "compute")
                .end().routingSlip(getSlipUri());
    }

    public void compute(@Headers Map headers) throws InterruptedException {
        //Thread.sleep(300);
        System.out.println(this.kind.name());
        if (Math.random() > 0) {
            ((Map<SourceKind, Boolean>) headers.get("notation")).put(this.kind, true);
        } else {
            ((Map<SourceKind, Boolean>) headers.get("notation")).put(this.kind, false);
        }
    }

    public void buildEndpoint() {
        notateEndpoint = "seda:notate_" + kind.name();
        consumeEndpoint = "seda:consume_" + kind.name();
    }

    public Expression getSlipUri() {
        return new Expression() {

            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) kind.slipUri;
            }
        };
    }

    synchronized public boolean checkReadyToConsume(@Headers Map headers) {
        Map<SourceKind, Boolean> notation = (Map<SourceKind, Boolean>) headers.get("notation");
        if (notation == null || notation.isEmpty()) {
            return true;
        } else {
            mergeNotation(arrivedNotation, notation);
            boolean flag;
            if (arrivedNotation.keySet().containsAll(this.kind.need)) {
                flag = true;
            } else {
                Set<SourceKind> affectableKind = SourceKind.getAffectableKind(arrivedNotation.keySet());
                flag = true;
                for (SourceKind k : this.kind.need) {
                    if (affectableKind.contains(k)) {
                        if (!arrivedNotation.containsKey(k)) {
                            flag = false;
                            break;
                        }
                    }
                }
            }
            if (flag) {
                return arrivedNotation.containsValue(true);
            } else {
                return false;
            }
        }
    }

    synchronized public void mergeNotation(Map<SourceKind, Boolean> notation_a, Map<SourceKind, Boolean> notation_b) {
        for (Map.Entry<SourceKind, Boolean> entry : notation_b.entrySet()) {
            SourceKind key = entry.getKey();
            Boolean b = notation_a.get(key);
            if (b == null) {
                notation_a.put(key, entry.getValue());
            } else {
                notation_a.put(key, b || entry.getValue());
            }
        }
    }

    synchronized public boolean checkShouldCompute(@Headers Map headers) {
        boolean shouldCompute = false;
        Map<SourceKind, Boolean> notation = (Map<SourceKind, Boolean>) headers.get("notation");
        if (notation == null) {
            headers.put("notation", new EnumMap<SourceKind, Boolean>(SourceKind.class));
            shouldCompute = true;
        } else if (notation.isEmpty()) {
            shouldCompute = true;
        } else {
            for (SourceKind k : this.kind.need) {
                Boolean b = arrivedNotation.get(k);
                if (b != null && b == true) {
                    shouldCompute = true;
                    break;
                }
            }
        }
        Map<SourceKind, Boolean> newNotation = new EnumMap<>(SourceKind.class);
        newNotation.putAll(arrivedNotation);
        newNotation.put(this.kind, false);
        headers.put("notation", newNotation);
        arrivedNotation.clear();
        return shouldCompute;
    }
}
