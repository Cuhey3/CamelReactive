package com.mycode.myreactivecamel;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public enum SourceKind {

    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P;
    final Set<SourceKind> need = new TreeSet<>();
    final Set<SourceKind> isRequiredTo = new TreeSet<>();
    final Set<SourceKind> affect = new TreeSet<>();
    final Set<SourceKind> influencedBy = new TreeSet<>();
    final Set<String> slipUri = new TreeSet<>();

    public void need(SourceKind kind) {
        if (!this.equals(kind) && !this.need.contains(kind)) {
            this.need.add(kind);
            kind.isRequiredTo(this);
        }
    }

    public void isRequiredTo(SourceKind kind) {
        if (!this.equals(kind) && !this.isRequiredTo.contains(kind)) {
            this.isRequiredTo.add(kind);
            kind.need(this);
        }
    }

    public static void calculateRelation() throws Exception {
        for (SourceKind k : SourceKind.values()) {
            k.setInfluencedBy(k.influencedBy, k);
            k.setAffect(k.affect, k);
            for (SourceKind from : k.need) {
                int count = k.countPath(from, 0);
                //System.out.println("from:" + from.name() + " to:" + k.name() + " path:" + count);
                if (count == 1) {
                    from.slipUri.add("seda:notate_" + k.name());
                    System.out.println("from: " + from.name() + " to: " + k.name());
                }
            }
            /*System.out.println(k.name() + " isRequiredTo:" + k.isRequiredTo);
             System.out.println(k.name() + " affect:" + k.affect);
             System.out.println(k.name() + " need:" + k.need);
             System.out.println(k.name() + " influencedBy:" + k.influencedBy);*/
        }
    }

    public void setInfluencedBy(Set<SourceKind> influence, SourceKind startBy) {
        for (SourceKind k : this.need) {
            if (!influence.contains(k) && !startBy.equals(k)) {
                influence.add(k);
                k.setInfluencedBy(influence, startBy);
            }
        }
    }

    public void setAffect(Set<SourceKind> affect, SourceKind startBy) {
        for (SourceKind k : this.isRequiredTo) {
            if (!affect.contains(k) && !startBy.equals(k)) {
                affect.add(k);
                k.setAffect(affect, startBy);
            }
        }
    }

    public static Set<SourceKind> getAffectableKind(Set<SourceKind> notated) {
        Set<SourceKind> set = new HashSet<>();
        for (SourceKind k : notated) {
            set.addAll(k.affect);
        }
        return set;
    }

    public int countPath(SourceKind from, int count) throws Exception {
        try {
            for (SourceKind k : this.need) {
                if (k.equals(from)) {
                    count++;
                } else {
                    count = k.countPath(from, count);
                }
            }

            return count;
        } catch (Throwable t) {
            System.out.println(this.name() + " " + from.name());
            throw new StackOverflowError();
        }
    }
}
