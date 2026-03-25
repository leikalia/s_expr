package org.example.sexpr.model;

import org.example.sexpr.ast.*;

import java.util.*;

public final class NodeView {
    private NodeView() {}

    public static boolean isElement(SNode node) {
        if (!(node instanceof SList list)) return false;
        if (list.items().isEmpty()) return false;
        return list.items().get(0) instanceof SSymbol;
    }

    public static String tag(SList element) {
        requireElement(element);
        return ((SSymbol) element.items().get(0)).value();
    }

    public static Map<String, SNode> attrs(SList element) {
        requireElement(element);

        Map<String, SNode> out = new LinkedHashMap<>();
        List<SNode> items = element.items();

        for (int i = 1; i < items.size(); i++) {
            SNode n = items.get(i);
            if (n instanceof SSymbol sym && sym.isAttributeName()) {
                if (i + 1 >= items.size()) {
                    throw new IllegalStateException("Attribute " + sym.value() + " has no value");
                }
                SNode value = items.get(i + 1);
                out.put(sym.value(), value);
                i++;
            }
        }
        return Collections.unmodifiableMap(out);
    }

    public static Optional<SNode> attr(SList element, String attrName) {
        return Optional.ofNullable(attrs(element).get(attrName));
    }

    public static boolean hasAttr(SList element, String attrName) {
        return attrs(element).containsKey(attrName);
    }

    public static List<SList> children(SList element) {
        requireElement(element);

        List<SList> out = new ArrayList<>();
        List<SNode> items = element.items();

        for (int i = 1; i < items.size(); i++) {
            SNode n = items.get(i);

            if (n instanceof SSymbol sym && sym.isAttributeName()) {
                if (i + 1 >= items.size()) {
                    throw new IllegalStateException("Attribute " + sym.value() + " has no value");
                }
                i++;
                continue;
            }

            if (n instanceof SList list && isElement(list)) {
                out.add(list);
            }
        }
        return List.copyOf(out);
    }

    public static List<SList> childrenByTag(SList element, String tag) {
        List<SList> all = children(element);
        List<SList> out = new ArrayList<>();
        for (SList ch : all) {
            if (tag(ch).equals(tag)) out.add(ch);
        }
        return List.copyOf(out);
    }

    private static void requireElement(SList element) {
        if (!isElement(element)) {
            throw new IllegalArgumentException("Expected element-like list: first item must be a symbol tag");
        }
    }
}
