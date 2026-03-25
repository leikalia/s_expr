package org.example.sexpr.update;

import org.example.sexpr.ast.SNode;

public record UpdateResult(SNode newRoot, int affectedCount) {
}
