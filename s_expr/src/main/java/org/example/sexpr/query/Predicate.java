package org.example.sexpr.query;

import org.example.sexpr.ast.SList;

@FunctionalInterface
public interface Predicate {
    boolean test(SList node);
}
