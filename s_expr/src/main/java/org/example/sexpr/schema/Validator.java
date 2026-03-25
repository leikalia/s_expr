package org.example.sexpr.schema;

import org.example.sexpr.ast.*;
import org.example.sexpr.model.NodeView;

import java.util.*;

public final class Validator {

    public ValidationResult validate(SNode documentRootAst, Schema schema) {
        Objects.requireNonNull(schema);

        if (!(documentRootAst instanceof SList root) || !NodeView.isElement(root)) {
            return new ValidationResult(false, List.of(new Violation("/", "Document root must be an element")));
        }

        String rootTag = NodeView.tag(root);
        List<Violation> violations = new ArrayList<>();

        if (!rootTag.equals(schema.rootTag())) {
            violations.add(new Violation("/" + rootTag, "Root tag mismatch: expected " + schema.rootTag() + ", got " + rootTag));
            return new ValidationResult(false, List.copyOf(violations));
        }

        validateElement(root, schema, "/" + rootTag, violations);
        return new ValidationResult(violations.isEmpty(), List.copyOf(violations));
    }

    private void validateElement(SList node, Schema schema, String path, List<Violation> violations) {
        String tag = NodeView.tag(node);
        ElementRule rule = schema.ruleFor(tag);

        if (rule == null) {
            violations.add(new Violation(path, "No schema rule for element '" + tag + "'"));
            return;
        }

        Map<String, SNode> attrs = NodeView.attrs(node);

        for (AttrRule ar : rule.attrs().values()) {
            SNode actual = attrs.get(ar.name());
            if (actual == null) {
                if (ar.required()) {
                    violations.add(new Violation(path, "Missing required attribute " + ar.name()));
                }
                continue;
            }

            if (!typeMatches(actual, ar.type())) {
                violations.add(new Violation(path, "Attribute " + ar.name() + " has wrong type: expected " + ar.type() + ", got " + actual.getClass().getSimpleName()));
            }
        }

        for (String seen : attrs.keySet()) {
            if (!rule.attrs().containsKey(seen)) {
                violations.add(new Violation(path, "Unknown attribute " + seen));
            }
        }

        List<SList> kids = NodeView.children(node);

        Map<String, Integer> counts = new HashMap<>();
        for (SList ch : kids) {
            String ct = NodeView.tag(ch);
            counts.put(ct, counts.getOrDefault(ct, 0) + 1);
        }

        Map<String, Integer> indexByTag = new HashMap<>();
        for (SList ch : kids) {
            String ct = NodeView.tag(ch);

            ChildRule cr = rule.children().get(ct);
            if (cr == null) {
                violations.add(new Violation(path, "Child element '" + ct + "' is not allowed here"));
            }

            int idx = indexByTag.getOrDefault(ct, 0) + 1;
            indexByTag.put(ct, idx);

            String childPath = path + "/" + ct + "[" + idx + "]";
            validateElement(ch, schema, childPath, violations);
        }

        for (ChildRule cr : rule.children().values()) {
            int c = counts.getOrDefault(cr.tag(), 0);
            if (c < cr.minOccurs()) {
                violations.add(new Violation(path, "Too few '" + cr.tag() + "': minOccurs=" + cr.minOccurs() + ", actual=" + c));
            }
            if (!cr.isUnbounded() && c > cr.maxOccurs()) {
                violations.add(new Violation(path, "Too many '" + cr.tag() + "': maxOccurs=" + cr.maxOccurs() + ", actual=" + c));
            }
        }
    }

    private boolean typeMatches(SNode actual, ValueType expected) {
        return switch (expected) {
            case ANY -> true;
            case STRING -> actual instanceof SString;
            case NUMBER -> actual instanceof SNumber;
            case BOOL -> actual instanceof SBool;
            case NULL -> actual instanceof SNull;
            case SYMBOL -> actual instanceof SSymbol;
        };
    }
}
