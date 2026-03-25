package org.example.sexpr.ast;

import java.math.BigDecimal;
import java.util.Objects;

public final class SNumber implements SAtom {
    private final BigDecimal value;

    public SNumber(BigDecimal value) {
        this.value = Objects.requireNonNull(value);
    }

    public BigDecimal value() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof SNumber other) && value.compareTo(other.value) == 0;
    }

    @Override public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override public String toString() { return "SNumber(" + value + ")"; }
}