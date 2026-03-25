package org.example.sexpr.schema;

import java.util.Objects;

public record ChildRule(String tag, int minOccurs, Integer maxOccurs) {

    public ChildRule {
        Objects.requireNonNull(tag);
        if (tag.isBlank()) throw new IllegalArgumentException("child tag is blank");
        if (minOccurs < 0) throw new IllegalArgumentException("minOccurs < 0");
        if (maxOccurs != null && maxOccurs < minOccurs) {
            throw new IllegalArgumentException("maxOccurs < minOccurs");
        }
    }

    public boolean isUnbounded() {
        return maxOccurs == null;
    }
}
