package org.example.sexpr;

import org.example.sexpr.fluent.QueryDsl;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.schema.SchemaParser;
import org.example.sexpr.schema.Schema;
import org.example.sexpr.schema.Validator;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.Updater;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommissionShowcaseTest {

    private final SExprParser parser = new SExprParser();
    private final SExprPrinter printer = new SExprPrinter();
    private final QueryEngine engine = new QueryEngine();
    private final Updater updater = new Updater();

    private Schema demoSchema() {
        var schemaAst = parser.parse(
                "(schema " +
                        "(root root) " +
                        "(element root (attrs) (children (users 1 1) (meta 0 1))) " +
                        "(element users (attrs) (children (user 1 N))) " +
                        "(element user (attrs (:id number required) (:active bool optional) (:name string optional)) (children)) " +
                        "(element meta (attrs) (children))" +
                        ")"
        );
        return new SchemaParser().parse(schemaAst);
    }

    @Test
    void endToEndShowcase() {
        var doc = parser.parse(
                "(root " +
                        "(users " +
                        "(user :id 1 :active true :name \"Ann\") " +
                        "(user :id 2 :active false :name \"Bob\") " +
                        "(user :id 3 :active true) " +
                        ") " +
                        "(meta) " +
                        ")"
        );
        System.out.println(printer.print(doc));
        var schema = demoSchema();
        var v1 = new Validator().validate(doc, schema);
        assertTrue(v1.ok(), () -> "Violations: " + v1.violations());

        assertEquals(2, engine.find(doc, "/root//user[:active=true]").size());

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":active", true).done()
                .build();

        assertEquals(2, q.find(doc).size());

        var res = updater.apply(doc, "/root//user[:id=3]", Mutation.setAttr(":active", new org.example.sexpr.ast.SBool(false)));
        System.out.println(printer.print(res.newRoot()));
        assertEquals(1, res.affectedCount());

        var doc2 = res.newRoot();
        assertEquals(1, engine.find(doc2, "/root//user[:active=true]").size());

        var text = printer.print(doc2);
        var doc3 = parser.parse(text);
        assertEquals(doc2, doc3);

        var v2 = new Validator().validate(doc2, schema);
        assertTrue(v2.ok(), () -> "Violations after update: " + v2.violations());
    }
}
