package org.example.sexpr.ast;

import java.util.Objects;

public final class SSymbol implements SAtom {
    private final String value;

    public SSymbol(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("symbol cannot be null/blank");
        this.value = value;
    }

    public String value() { return value; }

    public boolean isAttributeName() {
        return value.startsWith(":") && value.length() > 1;
    }

    @Override public boolean equals(Object o) {
        return (o instanceof SSymbol other) && Objects.equals(value, other.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public String toString() { return "SSymbol(" + value + ")"; }
}