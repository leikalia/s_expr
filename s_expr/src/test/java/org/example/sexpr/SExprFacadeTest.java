package org.example.sexpr;

import org.example.sexpr.ast.SBool;
import org.example.sexpr.update.Mutation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SExprFacadeTest {

    private static final String DOC =
            "(root " +
            "(users " +
            "(user :id 1 :active true  :name \"Ann\") " +
            "(user :id 2 :active false :name \"Bob\") " +
            "(user :id 3 :active true  :name \"Eva\") " +
            ") " +
            "(meta) " +
            ")";

    private static final String SCHEMA_TEXT =
            "(schema " +
            "(root root) " +
            "(element root  (attrs) (children (users 1 1) (meta 0 1))) " +
            "(element users (attrs) (children (user 1 N))) " +
            "(element user  (attrs (:id number required) (:active bool optional) (:name string optional)) (children)) " +
            "(element meta  (attrs) (children)) " +
            ")";


    @Test
    void parseAndPrint() {
        var doc = SExpr.parse(DOC);
        assertNotNull(doc);
        String printed = SExpr.print(doc);
        assertTrue(printed.startsWith("(root"));
    }

    @Test
    void normalizeIsIdempotent() {
        String once  = SExpr.normalize(DOC);
        String twice = SExpr.normalize(once);
        assertEquals(once, twice);
    }


    @Test
    void findAllUsers() {
        var doc = SExpr.parse(DOC);
        assertEquals(3, SExpr.find(doc, "/root//user").size());
    }

    @Test
    void findActiveUsers() {
        var doc = SExpr.parse(DOC);
        assertEquals(2, SExpr.find(doc, "/root//user[:active=true]").size());
    }


    @Test
    void deactivateUser() {
        var doc = SExpr.parse(DOC);
        var result = SExpr.update(doc, "//user[:id=1]", Mutation.setAttr(":active", new SBool(false)));
        assertEquals(1, result.affectedCount());
        assertEquals(1, SExpr.find(result.newRoot(), "//user[:active=true]").size());
    }

    @Test
    void deleteUser() {
        var doc = SExpr.parse(DOC);
        var result = SExpr.update(doc, "//user[:id=2]", Mutation.delete());
        assertEquals(1, result.affectedCount());
        assertEquals(2, SExpr.find(result.newRoot(), "//user").size());
    }


    @Test
    void fluentDslFindsActiveUsers() {
        var doc = SExpr.parse(DOC);
        var matches = SExpr.query()
                .root().child("root").desc("user")
                .where().attrEq(":active", true).done()
                .build()
                .find(doc);
        assertEquals(2, matches.size());
    }


    @Test
    void validDocPassesSchema() {
        var doc    = SExpr.parse(DOC);
        var schema = SExpr.parseSchema(SExpr.parse(SCHEMA_TEXT));
        var result = SExpr.validate(doc, schema);
        assertTrue(result.ok(), () -> "Violations: " + result.violations());
    }

    @Test
    void invalidDocFailsSchema() {
        var bad = SExpr.parse("(root (users (user :name \"Ghost\")) (meta))");
        var schema = SExpr.parseSchema(SExpr.parse(SCHEMA_TEXT));
        var result = SExpr.validate(bad, schema);
        assertFalse(result.ok());
    }
}
