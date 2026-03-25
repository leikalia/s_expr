package org.example.sexpr;

import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.fluent.QueryDsl;
import org.example.sexpr.query.QueryEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluentApiTest {

    @Test
    void fluentFindEqualsStringFind() {
        var doc = new SExprParser().parse(
                "(root (maxverstappen (supermax :id 1) (supermax :id 33)) (tutududu))"
        );

        var engine = new QueryEngine();

        
        var byString = engine.find(doc, "/root//supermax[:id=33]").size();

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("supermax")
                .where().attrEq(":id", 33).done()
                .build();

        var byFluent = q.find(doc).size();

        assertEquals(byString, byFluent);
        assertEquals(1, byFluent);
    }

    @Test
    void fluentUpdateWorks() {
        var parser = new SExprParser();
        var printer = new SExprPrinter();

        var doc = parser.parse(
                "(root (user :id 1 :active true) (user :id 2 :active true))"
        );

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":id", 2).done()
                .build();

        var res = q.update()
                .setAttr(":active", false)
                .apply(doc);

        assertEquals(1, res.affectedCount());

        var text = printer.print(res.newRoot());
        assertTrue(text.contains("(user :id 1 :active true)"));
        assertTrue(text.contains("(user :id 2 :active false)"));
    }

    @Test
    void fluentDeleteWorks() {
        var parser = new SExprParser();
        var printer = new SExprPrinter();

        var doc = parser.parse("(root (a) (b) (c))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .child("b")
                .done()
                .build();

        var res = q.delete().apply(doc);
        assertEquals(1, res.affectedCount());

        var text = printer.print(res.newRoot());
        assertTrue(text.contains("(a)"));
        assertFalse(text.contains("(b)"));
        assertTrue(text.contains("(c)"));
    }
}
