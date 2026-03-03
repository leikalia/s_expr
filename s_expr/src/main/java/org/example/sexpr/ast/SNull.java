package org.example.sexpr.ast;

public final class SNull implements SAtom {
    public static final SNull INSTANCE = new SNull();
    private SNull() {}

    @Override public boolean equals(Object o) { return o instanceof SNull; }
    @Override public int hashCode() { return 0; }

    @Override public String toString() { return "SNull"; }
}