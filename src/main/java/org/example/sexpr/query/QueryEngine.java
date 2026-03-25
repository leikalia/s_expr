package org.example.sexpr.query;

import org.example.sexpr.ast.SList;
import org.example.sexpr.ast.SNode;
import org.example.sexpr.model.NodeView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class QueryEngine {

    public List<Match> find(SNode root, String path) {
        return find(ExecutionContext.fromDocument(root), path);
    }

    public List<Match> find(ExecutionContext ctx, String path) {
        var q = new PathParser().parse(path);
        return find(ctx, q);
    }

    public List<Match> find(ExecutionContext ctx, PathQuery query) {
        SList start = query.absolute() ? ctx.documentRoot() : ctx.contextNode();


        if (query.absolute() && query.steps().isEmpty()) {
            return List.of(new Match("/" + NodeView.tag(ctx.documentRoot()), ctx.documentRoot()));
        }

        List<State> states;

        if (query.absolute()) {
            states = startFromDocumentRootXpath(ctx.documentRoot(), query.steps());
        } else {
            states = List.of(new State("/" + NodeView.tag(start), start));
            states = applySteps(states, query.steps());
        }

        List<Match> out = new ArrayList<>();
        for (State st : states) out.add(new Match(st.path, st.node));
        return List.copyOf(out);
    }

    private List<State> startFromDocumentRootXpath(SList documentRoot, List<Step> steps) {
        if (steps.isEmpty()) {
            return List.of(new State("/" + NodeView.tag(documentRoot), documentRoot));
        }

        Step first = steps.get(0);

        if (first.axis() == Axis.CHILD) {

            if (!matchesStep(documentRoot, first)) return List.of();

            List<State> states = List.of(new State("/" + NodeView.tag(documentRoot), documentRoot));
            return applySteps(states, steps.subList(1, steps.size()));
        } else {

            List<State> states = new ArrayList<>();
            String rootPath = "/" + NodeView.tag(documentRoot);

            if (matchesStep(documentRoot, first)) {
                states.add(new State(rootPath, documentRoot));
            }
            for (SList d : descendants(documentRoot)) {
                if (matchesStep(d, first)) {
                    states.add(new State(rootPath + "//" + NodeView.tag(d), d));
                }
            }
            return applySteps(states, steps.subList(1, steps.size()));
        }
    }

    private List<State> applySteps(List<State> states, List<Step> steps) {
        List<State> cur = states;

        for (Step step : steps) {
            List<State> next = new ArrayList<>();

            for (State st : cur) {
                if (step.axis() == Axis.CHILD) {
                    for (SList ch : NodeView.children(st.node)) {
                        if (matchesStep(ch, step)) {
                            next.add(new State(st.path + "/" + NodeView.tag(ch), ch));
                        }
                    }
                } else {
                    for (SList d : descendants(st.node)) {
                        if (matchesStep(d, step)) {
                            next.add(new State(st.path + "//" + NodeView.tag(d), d));
                        }
                    }
                }
            }

            cur = next;
            if (cur.isEmpty()) break;
        }

        return cur;
    }

    private boolean matchesStep(SList node, Step step) {

        boolean tagOk = step.isWildcard() || NodeView.tag(node).equals(step.tagOrWildcard());
        if (!tagOk) return false;

        for (Predicate p : step.predicates()) {
            if (!p.test(node)) return false;
        }
        return true;
    }

    private List<SList> descendants(SList start) {
        List<SList> out = new ArrayList<>();
        Deque<SList> dq = new ArrayDeque<>(NodeView.children(start));
        while (!dq.isEmpty()) {
            SList cur = dq.removeFirst();
            out.add(cur);
            for (SList ch : NodeView.children(cur)) dq.addLast(ch);
        }
        return out;
    }

    private record State(String path, SList node) {}
}

