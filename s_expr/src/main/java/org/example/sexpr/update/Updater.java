package org.example.sexpr.update;

import org.example.sexpr.ast.*;
import org.example.sexpr.model.NodeView;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.PathParser;
import org.example.sexpr.query.PathQuery;
import org.example.sexpr.query.QueryEngine;

import java.util.*;

public final class Updater {

    private final QueryEngine engine = new QueryEngine();

    public UpdateResult apply(SNode documentRoot, String path, Mutation mutation) {
        return apply(ExecutionContext.fromDocument(documentRoot), path, mutation);
    }

    public UpdateResult apply(ExecutionContext ctx, String path, Mutation mutation) {
        Objects.requireNonNull(path);
        var q = new PathParser().parse(path);
        return apply(ctx, q, mutation);
    }

    // NEW overload: apply by PathQuery (needed for fluent API)
    public UpdateResult apply(ExecutionContext ctx, PathQuery query, Mutation mutation) {
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(query);
        Objects.requireNonNull(mutation);

        var matches = engine.find(ctx, query);
        if (matches.isEmpty()) {
            return new UpdateResult(ctx.documentRoot(), 0);
        }

        Set<SList> targets = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var m : matches) targets.add(m.node());

        Counter counter = new Counter();
        SList newRoot = rebuild(ctx.documentRoot(), ctx.documentRoot(), targets, mutation, counter);

        return new UpdateResult(newRoot, counter.value);
    }

    private SList rebuild(SList current,
                          SList documentRoot,
                          Set<SList> targets,
                          Mutation mutation,
                          Counter counter) {

        List<SNode> items = current.items();
        List<SNode> rebuilt = new ArrayList<>();
        if (items.isEmpty()) return current;

        rebuilt.add(items.get(0)); // tag

        for (int i = 1; i < items.size(); i++) {
            SNode n = items.get(i);

            if (n instanceof SSymbol sym && sym.isAttributeName()) {
                if (i + 1 >= items.size()) throw new IllegalStateException("Attribute " + sym.value() + " has no value");
                rebuilt.add(n);
                rebuilt.add(items.get(i + 1));
                i++;
                continue;
            }

            if (n instanceof SList child && NodeView.isElement(child)) {
                SList rebuiltChild = rebuild(child, documentRoot, targets, mutation, counter);
                if (rebuiltChild != null) rebuilt.add(rebuiltChild);
                continue;
            }

            rebuilt.add(n);
        }

        SList rebuiltNode = new SList(rebuilt);

        if (targets.contains(current)) {
            if (mutation instanceof Mutation.Delete) {
                if (current == documentRoot) throw new IllegalArgumentException("Cannot delete documentRoot");
                counter.value++;
                return null;
            }

            if (mutation instanceof Mutation.ReplaceWith rep) {
                counter.value++;
                return rep.newNode();
            }

            if (mutation instanceof Mutation.SetAttr set) {
                counter.value++;
                return setAttrNormalized(rebuiltNode, set.attrName(), set.value());
            }

            if (mutation instanceof Mutation.RemoveAttr rem) {
                counter.value++;
                return removeAttrNormalized(rebuiltNode, rem.attrName());
            }
        }

        return rebuiltNode;
    }

    private SList setAttrNormalized(SList element, String attrName, SNode value) {
        List<SNode> items = element.items();
        if (!NodeView.isElement(element)) return element;

        SNode tag = items.get(0);
        List<SNode> attrs = new ArrayList<>();
        List<SNode> rest = new ArrayList<>();

        for (int i = 1; i < items.size(); i++) {
            SNode n = items.get(i);
            if (n instanceof SSymbol sym && sym.isAttributeName()) {
                if (i + 1 >= items.size()) throw new IllegalStateException("Attribute " + sym.value() + " has no value");
                SNode v = items.get(i + 1);
                if (!sym.value().equals(attrName)) {
                    attrs.add(sym);
                    attrs.add(v);
                }
                i++;
            } else {
                rest.add(n);
            }
        }

        attrs.add(new SSymbol(attrName));
        attrs.add(value);

        List<SNode> out = new ArrayList<>();
        out.add(tag);
        out.addAll(attrs);
        out.addAll(rest);
        return new SList(out);
    }

    private SList removeAttrNormalized(SList element, String attrName) {
        List<SNode> items = element.items();
        if (!NodeView.isElement(element)) return element;

        SNode tag = items.get(0);
        List<SNode> attrs = new ArrayList<>();
        List<SNode> rest = new ArrayList<>();

        for (int i = 1; i < items.size(); i++) {
            SNode n = items.get(i);
            if (n instanceof SSymbol sym && sym.isAttributeName()) {
                if (i + 1 >= items.size()) throw new IllegalStateException("Attribute " + sym.value() + " has no value");
                SNode v = items.get(i + 1);
                if (!sym.value().equals(attrName)) {
                    attrs.add(sym);
                    attrs.add(v);
                }
                i++;
            } else {
                rest.add(n);
            }
        }

        List<SNode> out = new ArrayList<>();
        out.add(tag);
        out.addAll(attrs);
        out.addAll(rest);
        return new SList(out);
    }

    private static final class Counter {
        int value = 0;
    }
}
