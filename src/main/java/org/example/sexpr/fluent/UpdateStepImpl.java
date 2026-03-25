package org.example.sexpr.fluent;

import org.example.sexpr.ast.*;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.PathQuery;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.UpdateResult;
import org.example.sexpr.update.Updater;

import java.math.BigDecimal;
import java.util.Objects;

final class UpdateStepImpl implements UpdateStep {

    private final PathQuery query;
    private final Updater updater;
    private Mutation mutation;

    UpdateStepImpl(PathQuery query, Updater updater) {
        this.query = Objects.requireNonNull(query);
        this.updater = Objects.requireNonNull(updater);
    }

    @Override
    public UpdateStep setAttr(String attrName, int value) {
        this.mutation = Mutation.setAttr(attrName, new SNumber(new BigDecimal(Integer.toString(value))));
        return this;
    }

    @Override
    public UpdateStep setAttr(String attrName, long value) {
        this.mutation = Mutation.setAttr(attrName, new SNumber(new BigDecimal(Long.toString(value))));
        return this;
    }

    @Override
    public UpdateStep setAttr(String attrName, BigDecimal value) {
        this.mutation = Mutation.setAttr(attrName, new SNumber(value));
        return this;
    }

    @Override
    public UpdateStep setAttr(String attrName, boolean value) {
        this.mutation = Mutation.setAttr(attrName, new SBool(value));
        return this;
    }

    @Override
    public UpdateStep setAttr(String attrName, String value) {
        this.mutation = Mutation.setAttr(attrName, new SString(value));
        return this;
    }

    @Override
    public UpdateStep setAttrNull(String attrName) {
        this.mutation = Mutation.setAttr(attrName, SNull.INSTANCE);
        return this;
    }

    @Override
    public UpdateStep removeAttr(String attrName) {
        this.mutation = Mutation.removeAttr(attrName);
        return this;
    }

    @Override
    public UpdateStep replaceWith(SList newNode) {
        this.mutation = Mutation.replaceWith(newNode);
        return this;
    }

    @Override
    public UpdateResult apply(SNode documentRoot) {
        return apply(ExecutionContext.fromDocument(documentRoot));
    }

    @Override
    public UpdateResult apply(ExecutionContext ctx) {
        if (mutation == null) {
            throw new IllegalStateException("No mutation configured. Call setAttr/removeAttr/replaceWith first.");
        }
        return updater.apply(ctx, query, mutation);
    }
}
