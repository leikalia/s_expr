package org.example.sexpr.fluent;

import org.example.sexpr.ast.*;
import org.example.sexpr.query.*;
import org.example.sexpr.update.Updater;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class Builder implements StartStep, PathStep, FilterStep, TerminalStep, FluentQuery {

    private boolean absolute;
    private final List<Step> steps = new ArrayList<>();

    private final QueryEngine engine = new QueryEngine();
    private final Updater updater = new Updater();

    @Override
    public PathStep root() { this.absolute = true; return this; }

    @Override
    public PathStep here() { this.absolute = false; return this; }

    @Override
    public PathStep child(String tag) {
        steps.add(new Step(Axis.CHILD, reqTag(tag), List.of()));
        return this;
    }

    @Override
    public PathStep anyChild() {
        steps.add(new Step(Axis.CHILD, "*", List.of()));
        return this;
    }

    @Override
    public PathStep desc(String tag) {
        steps.add(new Step(Axis.DESCENDANT, reqTag(tag), List.of()));
        return this;
    }

    @Override
    public PathStep anyDesc() {
        steps.add(new Step(Axis.DESCENDANT, "*", List.of()));
        return this;
    }

    @Override
    public FilterStep where() { ensureHasStep(); return this; }

    @Override
    public TerminalStep done() { return this; }

    @Override
    public FilterStep attrExists(String attrName) {
        ensureHasStep();
        addPredicate(new AttrExistsPredicate(reqAttr(attrName)));
        return this;
    }

    @Override
    public FilterStep attrEq(String attrName, int value) {
        return attrEqNode(attrName, new SNumber(new BigDecimal(Integer.toString(value))));
    }

    @Override
    public FilterStep attrEq(String attrName, long value) {
        return attrEqNode(attrName, new SNumber(new BigDecimal(Long.toString(value))));
    }

    @Override
    public FilterStep attrEq(String attrName, BigDecimal value) {
        return attrEqNode(attrName, new SNumber(value));
    }

    @Override
    public FilterStep attrEq(String attrName, boolean value) {
        return attrEqNode(attrName, new SBool(value));
    }

    @Override
    public FilterStep attrEq(String attrName, String value) {
        return attrEqNode(attrName, new SString(value));
    }

    @Override
    public FilterStep attrEqNull(String attrName) {
        return attrEqNode(attrName, SNull.INSTANCE);
    }

    private FilterStep attrEqNode(String attrName, SNode expected) {
        ensureHasStep();
        addPredicate(new AttrEqualsPredicate(reqAttr(attrName), expected));
        return this;
    }

    @Override
    public FluentQuery build() { return this; }

    @Override
    public List<FluentMatch> find(SNode documentRoot) {
        return find(ExecutionContext.fromDocument(documentRoot));
    }

    @Override
    public List<FluentMatch> find(ExecutionContext ctx) {
        var res = engine.find(ctx, toPathQuery());
        List<FluentMatch> out = new ArrayList<>();
        for (var m : res) out.add(new FluentMatch(m.path(), m.node()));
        return List.copyOf(out);
    }

    @Override
    public UpdateStep update() {
        return new UpdateStepImpl(toPathQuery(), updater);
    }

    @Override
    public DeleteStep delete() {
        return new DeleteStepImpl(toPathQuery(), updater);
    }

    @Override
    public PathQuery toPathQuery() {
        return new PathQuery(absolute, List.copyOf(steps));
    }

    private void addPredicate(org.example.sexpr.query.Predicate p) {
        int last = steps.size() - 1;
        Step prev = steps.get(last);
        List<org.example.sexpr.query.Predicate> preds = new ArrayList<>(prev.predicates());
        preds.add(p);
        steps.set(last, new Step(prev.axis(), prev.tagOrWildcard(), preds));
    }

    private void ensureHasStep() {
        if (steps.isEmpty()) throw new IllegalStateException("No path steps yet. Call child()/desc() before where().");
    }

    private static String reqTag(String tag) {
        if (tag == null || tag.isBlank()) throw new IllegalArgumentException("tag is null/blank");
        return tag;
    }

    private static String reqAttr(String a) {
        if (a == null) throw new IllegalArgumentException("attrName is null");
        if (!a.startsWith(":") || a.length() == 1) throw new IllegalArgumentException("attrName must look like :id");
        return a;
    }
}
