package org.example.sexpr.fluent;

import org.example.sexpr.ast.SNode;
import org.example.sexpr.query.ExecutionContext;

import java.util.List;

public interface TerminalStep {
    FluentQuery build();

    List<FluentMatch> find(SNode documentRoot);
    List<FluentMatch> find(ExecutionContext ctx);

    UpdateStep update();
    DeleteStep delete();
}
