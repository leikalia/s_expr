package org.example.sexpr;

import org.example.sexpr.ast.SBool;
import org.example.sexpr.ast.SList;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.query.ExecutionContext;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.fluent.QueryDsl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluentApiMoreTests {

    private final SExprParser parser = new SExprParser();
    private final SExprPrinter printer = new SExprPrinter();
    private final QueryEngine engine = new QueryEngine();

    @Test
    void buildShortcutWorksWithoutDone() {
        var doc = parser.parse("(root (a) (b) (c))");

        
        var q = QueryDsl.start()
                .root()
                .child("root")
                .child("b")
                .build();

        assertEquals(1, q.find(doc).size());
    }

    @Test
    void rootAbsoluteMatchesStringPath() {
        var doc = parser.parse("(root (users (user :id 1) (user :id 2)) (meta))");

        var byString = engine.find(doc, "/root/users/user").size();

        var q = QueryDsl.start()
                .root()
                .child("root")
                .child("users")
                .child("user")
                .build();

        assertEquals(byString, q.find(doc).size());
        assertEquals(2, byString);
    }

    @Test
    void descendantSearchMatchesStringPath() {
        var doc = parser.parse("(root (a (x)) (b (x)) (c))");

        var byString = engine.find(doc, "/root//x").size();

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("x")
                .build();

        assertEquals(byString, q.find(doc).size());
        assertEquals(2, byString);
    }

    @Test
    void wildcardChildMatchesStringPath() {
        var doc = parser.parse("(root (a) (b) (c))");

        var byString = engine.find(doc, "/root/*").size();

        var q = QueryDsl.start()
                .root()
                .child("root")
                .anyChild()
                .build();

        assertEquals(byString, q.find(doc).size());
        assertEquals(3, byString);
    }

    @Test
    void wildcardDescendantMatchesStringPath() {
        var doc = parser.parse("(root (a (x)) (b (y)) (c))");

        var byString = engine.find(doc, "/root//*").size();

        var q = QueryDsl.start()
                .root()
                .child("root")
                .anyDesc()
                .build();

        assertEquals(byString, q.find(doc).size());
        
        assertEquals(5, byString);
    }

    @Test
    void attrExistsPredicateWorks() {
        var doc = parser.parse("(root (user :id 1) (user) (user :id 2))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrExists(":id").done()
                .build();

        assertEquals(2, q.find(doc).size());
    }

    @Test
    void attrEqNumberPredicateWorks() {
        var doc = parser.parse("(root (supermax :id 1) (supermax :id 33) (supermax :id 2))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("supermax")
                .where().attrEq(":id", 33).done()
                .build();

        assertEquals(1, q.find(doc).size());
    }

    @Test
    void attrEqBoolPredicateWorks() {
        var doc = parser.parse("(root (user :active true) (user :active false) (user :active true))");

        var qTrue = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":active", true).done()
                .build();

        var qFalse = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":active", false).done()
                .build();

        assertEquals(2, qTrue.find(doc).size());
        assertEquals(1, qFalse.find(doc).size());
    }

    @Test
    void attrEqStringPredicateWorks() {
        var doc = parser.parse("(root (user :name \"Ann\") (user :name \"Bob\") (user :name \"Ann\"))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":name", "Ann").done()
                .build();

        assertEquals(2, q.find(doc).size());
    }

    @Test
    void hereRelativeContextDiffersFromRootAbsolute() {
        var doc = parser.parse("(root (group (item) (item)) (group (item)))");

        
        var group1 = engine.find(doc, "/root/group").get(0).node();
        var ctx = ExecutionContext.of(doc, group1);

        
        var qRel = QueryDsl.start()
                .here()
                .child("item")
                .build();

        assertEquals(2, qRel.find(ctx).size());

        
        var qAbs = QueryDsl.start()
                .root()
                .child("root")
                .desc("item")
                .build();

        assertEquals(3, qAbs.find(doc).size());
    }

    @Test
    void updateSetAttrWorks() {
        var doc = parser.parse("(root (user :id 1 :active true) (user :id 2 :active true))");

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
    void updateRemoveAttrWorks() {
        var doc = parser.parse("(root (user :id 1 :name \"Ann\") (user :id 2 :name \"Bob\"))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":id", 2).done()
                .build();

        var res = q.update()
                .removeAttr(":name")
                .apply(doc);

        assertEquals(1, res.affectedCount());

        var text = printer.print(res.newRoot());
        assertTrue(text.contains("(user :id 1 :name \"Ann\")"));
        assertTrue(text.contains("(user :id 2)"));
    }

    @Test
    void updateReplaceWithWorks() {
        var doc = parser.parse("(root (item :id 1) (item :id 2))");
        var replacement = (SList) parser.parse("(item :id 999 :name \"X\")");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("item")
                .where().attrEq(":id", 2).done()
                .build();

        var res = q.update()
                .replaceWith(replacement)
                .apply(doc);

        assertEquals(1, res.affectedCount());

        var text = printer.print(res.newRoot());
        assertTrue(text.contains("(item :id 1)"));
        assertTrue(text.contains("(item :id 999 :name \"X\")"));
        assertFalse(text.contains(":id 2"));
    }

    @Test
    void deleteWorks() {
        var doc = parser.parse("(root (a) (b) (c))");

        var q = QueryDsl.start()
                .root()
                .child("root")
                .child("b")
                .build(); 

        var res = q.delete().apply(doc);
        assertEquals(1, res.affectedCount());

        var text = printer.print(res.newRoot());
        assertTrue(text.contains("(a)"));
        assertFalse(text.contains("(b)"));
        assertTrue(text.contains("(c)"));
    }

    @Test
    void fluentFindEqualsStringFindOnComplexQuery() {
        var doc = parser.parse(
                "(root " +
                        "(team (user :id 1 :active true) (user :id 2 :active false)) " +
                        "(archive (user :id 3 :active true))" +
                        ")"
        );

        
        var byString = engine.find(doc, "/root//user[:active=true]").size();

        
        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":active", true).done()
                .build();

        assertEquals(byString, q.find(doc).size());
        assertEquals(2, byString);
    }
}
