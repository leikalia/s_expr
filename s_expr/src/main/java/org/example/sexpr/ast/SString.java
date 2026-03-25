package org.example.sexpr.ast;

import java.util.Objects;

public final class SString implements SAtom {
    private final String value;

    public SString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof SString other) && Objects.equals(value, other.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public String toString() { return "SString(\"" + value + "\")"; }
}