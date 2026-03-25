package org.example.sexpr.fluent;

import org.example.sexpr.ast.SList;
import org.example.sexpr.ast.SNode;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.update.UpdateResult;

import java.math.BigDecimal;

public interface UpdateStep {
    UpdateStep setAttr(String attrName, int value);
    UpdateStep setAttr(String attrName, long value);
    UpdateStep setAttr(String attrName, BigDecimal value);
    UpdateStep setAttr(String attrName, boolean value);
    UpdateStep setAttr(String attrName, String value);
    UpdateStep setAttrNull(String attrName);

    UpdateStep removeAttr(String attrName);

    UpdateStep replaceWith(SList newNode);

    UpdateResult apply(SNode documentRoot);
    UpdateResult apply(ExecutionContext ctx);
}
