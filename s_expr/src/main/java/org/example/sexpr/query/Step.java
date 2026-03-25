package org.example.sexpr.query;

import java.util.List;

public record Step(Axis axis, String tagOrWildcard, List<Predicate> predicates) {

    public Step {
        if (tagOrWildcard == null || tagOrWildcard.isBlank()) {
            throw new IllegalArgumentException("tagOrWildcard cannot be null/blank");
        }
        predicates = (predicates == null) ? List.of() : List.copyOf(predicates);
    }

    public Step(Axis axis, String tagOrWildcard) {
        this(axis, tagOrWildcard, List.of());
    }

    public boolean isWildcard() {
        return "*".equals(tagOrWildcard);
    }
}
