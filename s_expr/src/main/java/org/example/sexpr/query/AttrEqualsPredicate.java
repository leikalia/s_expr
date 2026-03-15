package org.example.sexpr.query;

import org.example.sexpr.ast.*;
import org.example.sexpr.model.NodeView;

import java.util.Objects;
import java.util.Optional;

public record AttrEqualsPredicate(String attrName, SNode expected) implements Predicate {

    public AttrEqualsPredicate {
        Objects.requireNonNull(attrName);
        Objects.requireNonNull(expected);
    }

    @Override
    public boolean test(SList node) {
        Optional<SNode> valOpt = NodeView.attr(node, attrName);
        if (valOpt.isEmpty()) return false;
        SNode actual = valOpt.get();
        return equalsByValue(actual, expected);
    }

    private boolean equalsByValue(SNode actual, SNode expected) {
        if (actual instanceof SNumber a && expected instanceof SNumber e) {
            return a.value().compareTo(e.value()) == 0;
        }
        if (actual instanceof SString a && expected instanceof SString e) {
            return a.value().equals(e.value());
        }
        if (actual instanceof SBool a && expected instanceof SBool e) {
            return a.value() == e.value();
        }
        if (actual instanceof SNull && expected instanceof SNull) {
            return true;
        }
        if (actual instanceof SSymbol a && expected instanceof SSymbol e) {
            return a.value().equals(e.value());
        }
        return false;
    }
}
