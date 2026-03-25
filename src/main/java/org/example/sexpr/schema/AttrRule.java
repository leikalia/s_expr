package org.example.sexpr.schema;

import java.util.Objects;

public record AttrRule(String name, ValueType type, boolean required) {

    public AttrRule {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        if (!name.startsWith(":") || name.length() == 1) {
            throw new IllegalArgumentException("Attribute name must look like :id, got: " + name);
        }
    }
}
