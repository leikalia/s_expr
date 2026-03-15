package org.example.sexpr;

import org.example.sexpr.ast.SBool;
import org.example.sexpr.ast.SNumber;
import org.example.sexpr.ast.SString;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.Updater;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTest {

    @Test
    void setAttrUpdatesAllMatches() {
        var doc = new SExprParser().parse(
                "(root (user :id 1 :active true) (user :id 2 :active true))"
        );

        var updater = new Updater();
        var res = updater.apply(doc, "//user", Mutation.setAttr(":active", new SBool(false)));

        assertEquals(2, res.affectedCount());
        var text = new SExprPrinter().print(res.newRoot());

        assertTrue(text.contains("(user :id 1 :active false)"));
        assertTrue(text.contains("(user :id 2 :active false)"));
    }

    @Test
    void removeAttrWorksWithPredicate() {
        var doc = new SExprParser().parse(
                "(root (user :id 1 :name \"Ann\") (user :id 2 :name \"Bob\"))"
        );

        var updater = new Updater();
        var res = updater.apply(doc, "//user[:id=2]", Mutation.removeAttr(":name"));

        assertEquals(1, res.affectedCount());
        var text = new SExprPrinter().print(res.newRoot());

        assertTrue(text.contains("(user :id 1 :name \"Ann\")"));
        assertTrue(text.contains("(user :id 2)")); // name удалили
    }

    @Test
    void replaceWithWorks() {
        var doc = new SExprParser().parse(
                "(root (item :id 1) (item :id 2))"
        );

        var replacement = (org.example.sexpr.ast.SList) new SExprParser().parse("(item :id 999 :name \"X\")");

        var updater = new Updater();
        var res = updater.apply(doc, "//item[:id=2]", Mutation.replaceWith(replacement));

        assertEquals(1, res.affectedCount());
        var text = new SExprPrinter().print(res.newRoot());

        assertTrue(text.contains("(item :id 1)"));
        assertTrue(text.contains("(item :id 999 :name \"X\")"));
        assertFalse(text.contains(":id 2"));
    }

    @Test
    void deleteRemovesNodes() {
        var doc = new SExprParser().parse(
                "(root (supermax :id 1) (supermax :id 33) (tutududu))"
        );

        var updater = new Updater();
        var res = updater.apply(doc, "//supermax[:id=33]", Mutation.delete());

        assertEquals(1, res.affectedCount());
        var text = new SExprPrinter().print(res.newRoot());

        assertTrue(text.contains("(supermax :id 1)"));
        assertFalse(text.contains(":id 33"));
        assertTrue(text.contains("(tutududu)"));
    }

    @Test
    void deleteDocumentRootIsError() {
        var doc = new SExprParser().parse("(root (a) (b))");
        var updater = new Updater();

        assertThrows(IllegalArgumentException.class, () ->
                updater.apply(doc, "/root", Mutation.delete())
        );
    }
}
