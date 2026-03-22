package org.example.sexpr.fluent;

import org.example.sexpr.ast.SNode;
import org.example.sexpr.query.ExecutionContext;

import java.util.List;

public interface PathStep {
    PathStep child(String tag);   // /tag
    PathStep anyChild();          // /*
    PathStep desc(String tag);    // //tag
    PathStep anyDesc();           // //*

    FilterStep where();
    TerminalStep done();

    default FluentQuery build() {
        return done().build();
    }

    default List<FluentMatch> find(SNode documentRoot) {
        return done().find(documentRoot);
    }

    default List<FluentMatch> find(ExecutionContext ctx) {
        return done().find(ctx);
    }

    default UpdateStep update() {
        return done().update();
    }

    default DeleteStep delete() {
        return done().delete();
    }
}
