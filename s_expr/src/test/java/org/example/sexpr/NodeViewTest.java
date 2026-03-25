package org.example.sexpr;

import org.example.sexpr.ast.*;
import org.example.sexpr.model.NodeView;
import org.example.sexpr.parse.SExprParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NodeViewTest {

    @Test
    void tagIsFirstSymbol() {
        var root = (SList) new SExprParser().parse("(user :id 1 :active true)");
        assertEquals("user", NodeView.tag(root));
    }

    @Test
    void attrsAreParsedAsPairs() {
        var root = (SList) new SExprParser().parse("(user :id 1 :active true :name \"Ann\")");

        var attrs = NodeView.attrs(root);
        assertEquals(3, attrs.size());

        assertEquals(new SNumber(new BigDecimal("1")), attrs.get(":id"));
        assertEquals(new SBool(true), attrs.get(":active"));
        assertEquals(new SString("Ann"), attrs.get(":name"));

        assertTrue(NodeView.hasAttr(root, ":id"));
        assertFalse(NodeView.hasAttr(root, ":missing"));
    }

    @Test
    void childrenExcludeAttributeValueLists() {
        
        
        var root = (SList) new SExprParser().parse("(user :meta (x y) (child :k 1) (child :k 2))");

        var kids = NodeView.children(root);
        assertEquals(2, kids.size());
        assertEquals("child", NodeView.tag(kids.get(0)));
        assertEquals("child", NodeView.tag(kids.get(1)));
    }

    @Test
    void childrenByTagFilters() {
        var root = (SList) new SExprParser().parse("(root (a) (b) (a :x 1))");

        var aKids = NodeView.childrenByTag(root, "a");
        assertEquals(2, aKids.size());
        assertEquals("a", NodeView.tag(aKids.get(0)));
        assertEquals("a", NodeView.tag(aKids.get(1)));
    }

    @Test
    void elementRequiresFirstItemSymbol() {
        var notElement = new SList(java.util.List.of(new SNumber(new BigDecimal("1"))));
        assertFalse(NodeView.isElement(notElement));
        assertThrows(IllegalArgumentException.class, () -> NodeView.tag(notElement));
    }

    @Test
    void attributeWithoutValueIsError() {
        var root = (SList) new SExprParser().parse("(user :id)");
        
        assertThrows(IllegalStateException.class, () -> NodeView.attrs(root));
        assertThrows(IllegalStateException.class, () -> NodeView.children(root));
    }
}
