package com.mycode.myreactivecamel;

import java.util.EnumMap;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;

public class App {

    public static void main(String[] args) throws Exception {
        SourceKind.B.need(SourceKind.A);
        SourceKind.B.need(SourceKind.D);
        SourceKind.B.need(SourceKind.K);
        SourceKind.B.need(SourceKind.N);
        SourceKind.C.need(SourceKind.G);
        SourceKind.E.need(SourceKind.D);
        SourceKind.E.need(SourceKind.L);
        SourceKind.H.need(SourceKind.D);
        SourceKind.H.need(SourceKind.O);
        SourceKind.I.need(SourceKind.C);
        SourceKind.I.need(SourceKind.F);
        SourceKind.I.need(SourceKind.M);
        SourceKind.J.need(SourceKind.D);
        SourceKind.J.need(SourceKind.G);
        SourceKind.J.need(SourceKind.L);
        SourceKind.K.need(SourceKind.H);
        SourceKind.K.need(SourceKind.O);
        SourceKind.L.need(SourceKind.F);
        SourceKind.L.need(SourceKind.G);
        SourceKind.L.need(SourceKind.N);
        SourceKind.M.need(SourceKind.A);
        SourceKind.M.need(SourceKind.K);
        SourceKind.M.need(SourceKind.O);
        SourceKind.N.need(SourceKind.H);
        SourceKind.N.need(SourceKind.I);
        SourceKind.O.need(SourceKind.A);
        SourceKind.calculateRelation();

        CamelContext context = new DefaultCamelContext();
        for (SourceKind k : SourceKind.values()) {
            ReactiveSource source = new ReactiveSource(k);
            source.buildEndpoint();
            context.addRoutes(source);
        }
        context.start();
        ProducerTemplate pt = context.createProducerTemplate();
        DefaultExchange exchange1 = new DefaultExchange(context);
        DefaultExchange exchange2 = new DefaultExchange(context);
        EnumMap<SourceKind, Boolean> map1 = new EnumMap<>(SourceKind.class);
        EnumMap<SourceKind, Boolean> map2 = new EnumMap<>(SourceKind.class);
        exchange1.getIn().setHeader("notation", map1);
        exchange2.getIn().setHeader("notation", map2);
        //pt.send("seda:start_C", exchange);
        pt.send("seda:notate_A", exchange1);
        /*        Thread.sleep(1000);
         pt.send("seda:notate_E", exchange2);*/
        Thread.sleep(Long.MAX_VALUE);

    }
}
