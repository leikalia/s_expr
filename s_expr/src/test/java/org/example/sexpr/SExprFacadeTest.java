package org.example.sexpr;

import org.example.sexpr.ast.SBool;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.Updater;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoShowcaseTest {

    @Test
    void showcase() {
        var parser = new SExprParser();
        var printer = new SExprPrinter();
        var engine = new QueryEngine();
        var updater = new Updater();

        var doc = parser.parse(
                "(root " +
                        "(maxverstappen (supermax :id 1 :active true) (supermax :id 33 :active true)) " +
                        "(tutududu)" +
                        ")"
        );

        assertEquals(2, engine.find(doc, "/root/maxverstappen/supermax").size());
        assertEquals(1, engine.find(doc, "//supermax[:id=33]").size());

        var max = engine.find(doc, "/root/maxverstappen").get(0).node();
        var ctx = ExecutionContext.of(doc, max);

        assertEquals(2, engine.find(ctx, "supermax").size());

        var res = updater.apply(doc, "//supermax[:id=33]", Mutation.setAttr(":active", new SBool(false)));
        assertEquals(1, res.affectedCount());

        System.out.println("UPDATED DOC:");
        System.out.println(printer.print(res.newRoot()));

        var res2 = updater.apply(res.newRoot(), "//supermax[:id=1]", Mutation.delete());
        assertEquals(1, res2.affectedCount());

        System.out.println("AFTER DELETE:");
        System.out.println(printer.print(res2.newRoot()));
    }
}
