package org.example.sexpr.ast;

public final class SBool implements SAtom {
    private final boolean value;

    public SBool(boolean value) { this.value = value; }

    public boolean value() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof SBool other) && value == other.value;
    }

    @Override public int hashCode() { return Boolean.hashCode(value); }

    @Override public String toString() { return "SBool(" + value + ")"; }
}