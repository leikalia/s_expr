package org.example.sexpr.query;

import org.example.sexpr.ast.SList;
import org.example.sexpr.ast.SNode;
import org.example.sexpr.model.NodeView;

public record ExecutionContext(SList documentRoot, SList contextNode) {

    public static ExecutionContext of(SNode documentRoot, SNode contextNode) {
        if (!(documentRoot instanceof SList dr) || !NodeView.isElement(dr)) {
            throw new IllegalArgumentException("documentRoot must be an element-like SList");
        }
        if (!(contextNode instanceof SList cn) || !NodeView.isElement(cn)) {
            throw new IllegalArgumentException("contextNode must be an element-like SList");
        }
        return new ExecutionContext(dr, cn);
    }

    public static ExecutionContext fromDocument(SNode documentRoot) {
        return of(documentRoot, documentRoot);
    }
}
