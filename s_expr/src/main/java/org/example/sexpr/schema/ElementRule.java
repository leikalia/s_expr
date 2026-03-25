package org.example.sexpr.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ElementRule {
    private final String tag;
    private final Map<String, AttrRule> attrs;
    private final Map<String, ChildRule> children;

    public ElementRule(String tag,
                       Map<String, AttrRule> attrs,
                       Map<String, ChildRule> children) {
        this.tag = Objects.requireNonNull(tag);
        if (tag.isBlank()) throw new IllegalArgumentException("tag is blank");
        this.attrs = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(attrs)));
        this.children = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(children)));
    }

    public String tag() { return tag; }

    public Map<String, AttrRule> attrs() { return attrs; }

    public Map<String, ChildRule> children() { return children; }
}
