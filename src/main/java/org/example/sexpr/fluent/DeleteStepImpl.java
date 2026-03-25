package org.example.sexpr.fluent;

import org.example.sexpr.ast.SNode;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.PathQuery;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.UpdateResult;
import org.example.sexpr.update.Updater;

import java.util.Objects;

final class DeleteStepImpl implements DeleteStep {

    private final PathQuery query;
    private final Updater updater;

    DeleteStepImpl(PathQuery query, Updater updater) {
        this.query = Objects.requireNonNull(query);
        this.updater = Objects.requireNonNull(updater);
    }

    @Override
    public UpdateResult apply(SNode documentRoot) {
        return apply(ExecutionContext.fromDocument(documentRoot));
    }

    @Override
    public UpdateResult apply(ExecutionContext ctx) {
        return updater.apply(ctx, query, Mutation.delete());
    }
}
