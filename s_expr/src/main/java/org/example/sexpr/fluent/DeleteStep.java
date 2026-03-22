package org.example.sexpr.fluent;

import org.example.sexpr.ast.SNode;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.update.UpdateResult;

public interface DeleteStep {
    UpdateResult apply(SNode documentRoot);
    UpdateResult apply(ExecutionContext ctx);
}
