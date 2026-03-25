package org.example.sexpr;

import org.example.sexpr.ast.SList;
import org.example.sexpr.fluent.QueryDsl;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.schema.Schema;
import org.example.sexpr.schema.SchemaParser;
import org.example.sexpr.schema.Validator;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.Updater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HardcoreStressTest {

    private final SExprParser parser = new SExprParser();
    private final SExprPrinter printer = new SExprPrinter();
    private final QueryEngine engine = new QueryEngine();
    private final Updater updater = new Updater();

    

    private String deepNestedDoc(int depth) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("(root ");
        for (int i = 0; i < depth; i++) sb.append("(a ");
        sb.append("(leaf :id 1)");
        for (int i = 0; i < depth; i++) sb.append(")");
        sb.append(")");
        return sb.toString();
    }

    @Test
    @Timeout(value = 6, unit = TimeUnit.SECONDS)
    void deepNestingDescendantSearchWorks() {
        var doc = parser.parse(deepNestedDoc(200));

        
        assertEquals(1, engine.find(doc, "/root//leaf").size());

        
        assertEquals(200, engine.find(doc, "/root//a").size());
    }

    

    @Test
    void stringsEscapingRoundTripWorks() {
        var text = "(root (msg :text \"line1\\nline2 \\\"quoted\\\" \\\\ slash\"))";
        var node1 = parser.parse(text);

        var printed = printer.print(node1);
        var node2 = parser.parse(printed);

        assertEquals(node1, node2);
    }

    

    private Schema strictSchema() {
        
        
        
        
        var schemaAst = parser.parse(
                "(schema " +
                        "(root root) " +
                        "(element root (attrs) (children (users 1 1))) " +
                        "(element users (attrs) (children (user 2 3))) " +
                        "(element user (attrs (:id number required) (:active bool optional)) (children))" +
                        ")"
        );
        return new SchemaParser().parse(schemaAst);
    }

    @Test
    void validatorCollectsManyViolations() {
        var schema = strictSchema();

        
        
        
        
        var badDoc = parser.parse(
                "(root " +
                        "(users (user :name \"Ann\" :active \"yes\")) " +
                        "(meta)" +
                        ")"
        );

        var res = new Validator().validate(badDoc, schema);
        assertFalse(res.ok());
        
        assertTrue(res.violations().size() >= 4, () -> "Violations: " + res.violations());

        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("not allowed")));
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("Too few 'user'")));
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("Missing required attribute :id")));
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("Unknown attribute :name")));
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("wrong type")));
    }

    

    private String manyUsersDoc(int n) {
        StringBuilder sb = new StringBuilder();
        sb.append("(root (users ");
        for (int i = 1; i <= n; i++) {
            sb.append("(user :id ").append(i).append(" :active ").append((i % 3 == 0) ? "true" : "false").append(")");
        }
        sb.append("))");
        return sb.toString();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deterministicFuzzUpdatesKeepDocumentParseable() {
        var doc = parser.parse(manyUsersDoc(2000));
        var rnd = new Random(42);

        
        assertEquals(2000, engine.find(doc, "/root//user").size());

        
        for (int step = 0; step < 50; step++) {
            int id = 1 + rnd.nextInt(2000);

            int op = rnd.nextInt(4);
            switch (op) {
                case 0 -> {
                    
                    doc = updater.apply(doc, "/root//user[:id=" + id + "]",
                            Mutation.setAttr(":active", new org.example.sexpr.ast.SBool(rnd.nextBoolean()))).newRoot();
                }
                case 1 -> {
                    
                    doc = updater.apply(doc, "/root//user[:id=" + id + "]",
                            Mutation.removeAttr(":active")).newRoot();
                }
                case 2 -> {
                    
                    doc = updater.apply(doc, "/root//user[:id=" + id + "]",
                            Mutation.delete()).newRoot();
                }
                case 3 -> {
                    
                    var repl = (SList) parser.parse("(user :id " + id + " :active true :name \"X\")");
                    doc = updater.apply(doc, "/root//user[:id=" + id + "]",
                            Mutation.replaceWith(repl)).newRoot();
                }
            }

            
            var printed = printer.print(doc);
            var reparsed = parser.parse(printed);
            assertEquals(doc, reparsed);

            
            var q = QueryDsl.start().root().child("root").desc("user").build();
            for (var m : q.find(doc)) {
                assertEquals("user", org.example.sexpr.model.NodeView.tag(m.node()));
            }
        }
    }
}
