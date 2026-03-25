package org.example.sexpr.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Schema {
    private final String rootTag;
    private final Map<String, ElementRule> elements;

    public Schema(String rootTag, Map<String, ElementRule> elements) {
        this.rootTag = Objects.requireNonNull(rootTag);
        this.elements = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(elements)));
        if (!this.elements.containsKey(rootTag)) {
            throw new IllegalArgumentException("Schema does not contain rule for rootTag=" + rootTag);
        }
    }

    public String rootTag() { return rootTag; }

    public Map<String, ElementRule> elements() { return elements; }

    public ElementRule ruleFor(String tag) {
        return elements.get(tag);
    }
}
