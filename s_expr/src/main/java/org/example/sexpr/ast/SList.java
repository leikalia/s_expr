package org.example.sexpr.ast;

import java.util.List;
import java.util.Objects;

public final class SList implements SNode {
    private final List<SNode> items;

    public SList(List<SNode> items) {
        this.items = List.copyOf(Objects.requireNonNull(items));
    }

    public List<SNode> items() {
        return items;
    }

    @Override public boolean equals(Object o) {
        return (o instanceof SList other) && items.equals(other.items);
    }

    @Override public int hashCode() { return items.hashCode(); }

    @Override public String toString() { return "SList" + items; }
}