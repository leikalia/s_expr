package org.example.sexpr.query;

import org.example.sexpr.ast.SList;
import org.example.sexpr.model.NodeView;

import java.util.Objects;

public record AttrExistsPredicate(String attrName) implements Predicate {

    public AttrExistsPredicate {
        Objects.requireNonNull(attrName);
    }

    @Override
    public boolean test(SList node) {
        return NodeView.hasAttr(node, attrName);
    }
}
