package org.example.sexpr;

import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.schema.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaValidationTest {

    private final SExprParser parser = new SExprParser();

    private Schema loadSchema() {
        var schemaAst = parser.parse(
                "(schema " +
                        "(root root) " +
                        "(element root " +
                        "(attrs) " +
                        "(children (users 1 1) (meta 0 1))" +
                        ") " +
                        "(element users " +
                        "(attrs) " +
                        "(children (user 1 N))" +
                        ") " +
                        "(element user " +
                        "(attrs (:id number required) (:active bool optional) (:name string optional)) " +
                        "(children)" +
                        ") " +
                        "(element meta (attrs) (children))" +
                        ")"
        );
        return new SchemaParser().parse(schemaAst);
    }

    @Test
    void validDocumentPasses() {
        var schema = loadSchema();
        var doc = parser.parse(
                "(root (users (user :id 1 :active true) (user :id 2 :name \"Ann\")) (meta))"
        );

        var res = new Validator().validate(doc, schema);
        assertTrue(res.ok(), () -> "Violations: " + res.violations());
        assertEquals(0, res.violations().size());
    }

    @Test
    void missingRequiredAttrFails() {
        var schema = loadSchema();
        var doc = parser.parse(
                "(root (users (user :active true)) )"
        );

        var res = new Validator().validate(doc, schema);
        assertFalse(res.ok());
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("Missing required attribute :id")));
    }

    @Test
    void wrongAttrTypeFails() {
        var schema = loadSchema();
        var doc = parser.parse(
                "(root (users (user :id \"oops\")) )"
        );

        var res = new Validator().validate(doc, schema);
        assertFalse(res.ok());
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("wrong type")));
    }

    @Test
    void unknownAttrFails() {
        var schema = loadSchema();
        var doc = parser.parse(
                "(root (users (user :id 1 :unknown 5)) )"
        );

        var res = new Validator().validate(doc, schema);
        assertFalse(res.ok());
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("Unknown attribute :unknown")));
    }

    @Test
    void childNotAllowedFails() {
        var schema = loadSchema();
        var doc = parser.parse(
                "(root (users (user :id 1)) (oops))"
        );

        var res = new Validator().validate(doc, schema);
        assertFalse(res.ok());
        assertTrue(res.violations().stream().anyMatch(v -> v.message().contains("not allowed")));
    }

    @Test
    void minMaxOccursEnforced() {
        var schema = loadSchema();

        
        var doc1 = parser.parse("(root (users) )");
        var res1 = new Validator().validate(doc1, schema);
        assertFalse(res1.ok());
        assertTrue(res1.violations().stream().anyMatch(v -> v.message().contains("Too few 'user'")));

        
        var doc2 = parser.parse("(root (users (user :id 1)) (meta) (meta))");
        var res2 = new Validator().validate(doc2, schema);
        assertFalse(res2.ok());
        assertTrue(res2.violations().stream().anyMatch(v -> v.message().contains("Too many 'meta'")));
    }
}
