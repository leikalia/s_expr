package org.example.sexpr;

import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.QueryEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XPathSemanticsTest {

    @Test
    void absoluteStartsFromDocumentRootRelativeFromContext() {
        var doc = new SExprParser().parse(
                "(root (maxverstappen (supermax :id 1) (supermax :id 33)) (tutududu))"
        );

        var engine = new QueryEngine();

        var users = engine.find(doc, "/root/maxverstappen").get(0).node();
        var ctx = ExecutionContext.of(doc, users);

        assertEquals(2, engine.find(ctx, "supermax").size());
        assertEquals(2, engine.find(ctx, "/root/maxverstappen/supermax").size());
        assertEquals(1, engine.find(ctx, "/root/tutududu").size());
    }
}
