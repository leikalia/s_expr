package org.example.sexpr.schema;

import org.example.sexpr.ast.*;
import org.example.sexpr.model.NodeView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SchemaParser {

    public Schema parse(SNode schemaAst) {
        if (!(schemaAst instanceof SList top) || !NodeView.isElement(top)) {
            throw new IllegalArgumentException("Schema must be an element-like list");
        }
        if (!NodeView.tag(top).equals("schema")) {
            throw new IllegalArgumentException("Top tag must be 'schema'");
        }

        String rootTag = null;
        Map<String, ElementRule> rules = new LinkedHashMap<>();

        for (SList child : NodeView.children(top)) {
            String tag = NodeView.tag(child);

            if (tag.equals("root")) {
                List<SNode> items = child.items();
                if (items.size() != 2 || !(items.get(1) instanceof SSymbol sym)) {
                    throw new IllegalArgumentException("(root <tag>) expected");
                }
                rootTag = sym.value();
                continue;
            }

            if (tag.equals("element")) {
                ElementRule rule = parseElementRule(child);
                rules.put(rule.tag(), rule);
                continue;
            }

            throw new IllegalArgumentException("Unknown schema directive: " + tag);
        }

        if (rootTag == null) {
            throw new IllegalArgumentException("Schema must contain (root <tag>)");
        }

        return new Schema(rootTag, rules);
    }

    private ElementRule parseElementRule(SList elementNode) {
        List<SNode> items = elementNode.items();
        if (items.size() < 2 || !(items.get(1) instanceof SSymbol tagSym)) {
            throw new IllegalArgumentException("(element <tag> ...) expected");
        }
        String tag = tagSym.value();

        Map<String, AttrRule> attrs = new LinkedHashMap<>();
        Map<String, ChildRule> children = new LinkedHashMap<>();

        for (int i = 2; i < items.size(); i++) {
            SNode n = items.get(i);
            if (!(n instanceof SList list) || !NodeView.isElement(list)) {
                throw new IllegalArgumentException("Expected (attrs ...) or (children ...) inside element " + tag);
            }
            String blockTag = NodeView.tag(list);

            if (blockTag.equals("attrs")) {
                attrs.putAll(parseAttrsBlock(list));
            } else if (blockTag.equals("children")) {
                children.putAll(parseChildrenBlock(list));
            } else {
                throw new IllegalArgumentException("Unknown element block: " + blockTag + " in element " + tag);
            }
        }

        return new ElementRule(tag, attrs, children);
    }

    private Map<String, AttrRule> parseAttrsBlock(SList attrsBlock) {
        Map<String, AttrRule> out = new LinkedHashMap<>();

        for (SList a : NodeView.children(attrsBlock)) {
            String attrName = NodeView.tag(a);
            if (!attrName.startsWith(":") || attrName.length() == 1) {
                throw new IllegalArgumentException("Attr spec tag must look like :id, got: " + attrName);
            }

            List<SNode> items = a.items();
            if (items.size() != 3) {
                throw new IllegalArgumentException("Attr spec must be (:name type required|optional)");
            }

            String typeText = readAtomAsText(items.get(1), "attr type");
            String reqText = readAtomAsText(items.get(2), "attr required/optional");

            ValueType type = parseType(typeText);
            boolean required = switch (reqText) {
                case "required" -> true;
                case "optional" -> false;
                default -> throw new IllegalArgumentException("Expected required|optional, got: " + reqText);
            };

            out.put(attrName, new AttrRule(attrName, type, required));
        }

        return out;
    }

    private Map<String, ChildRule> parseChildrenBlock(SList childrenBlock) {
        Map<String, ChildRule> out = new LinkedHashMap<>();

        for (SList c : NodeView.children(childrenBlock)) {
            String childTag = NodeView.tag(c);

            List<SNode> items = c.items();
            if (items.size() != 3) {
                throw new IllegalArgumentException("Child rule must be (tag min max)");
            }

            int min = readInt(items.get(1), "minOccurs");
            Integer max = readMax(items.get(2), "maxOccurs");

            out.put(childTag, new ChildRule(childTag, min, max));
        }

        return out;
    }

    private ValueType parseType(String t) {
        return switch (t) {
            case "string" -> ValueType.STRING;
            case "number" -> ValueType.NUMBER;
            case "bool" -> ValueType.BOOL;
            case "null" -> ValueType.NULL;
            case "symbol" -> ValueType.SYMBOL;
            case "any" -> ValueType.ANY;
            default -> throw new IllegalArgumentException("Unknown type: " + t);
        };
    }

    private String readAtomAsText(SNode n, String what) {
        if (n instanceof SSymbol s) return s.value();
        if (n instanceof SString s) return s.value();
        if (n instanceof SNumber num) return num.value().toPlainString();
        if (n instanceof SBool b) return b.value() ? "true" : "false";
        if (n instanceof SNull) return "null";
        throw new IllegalArgumentException("Expected atom for " + what);
    }

    private int readInt(SNode n, String what) {
        if (n instanceof SNumber num) {
            try {
                return num.value().intValueExact();
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Expected integer number for " + what);
            }
        }
        if (n instanceof SSymbol s) {
            return parseInt(s.value(), what);
        }
        throw new IllegalArgumentException("Expected number or symbol for " + what);
    }

    private Integer readMax(SNode n, String what) {
        if (n instanceof SSymbol s) {
            String v = s.value();
            if (v.equals("N") || v.equals("*")) return null;
            return parseInt(v, what);
        }
        if (n instanceof SNumber num) {
            try {
                return num.value().intValueExact();
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Expected integer number for " + what);
            }
        }
        throw new IllegalArgumentException("Expected number or symbol for " + what);
    }

    private int parseInt(String s, String what) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Expected integer for " + what + ", got: " + s);
        }
    }
}
