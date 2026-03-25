package org.example.sexpr;

import org.example.sexpr.ast.*;
import org.example.sexpr.parse.SExprParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SExprParserTest {

    @Test
    void parsesListWithAtoms() {
        var node = new SExprParser().parse("(user :id 1 :active true :note \"hi\")");
        assertTrue(node instanceof SList);

        var list = (SList) node;
        assertEquals(7, list.items().size());

        assertEquals(new SSymbol("user"), list.items().get(0));
        assertEquals(new SSymbol(":id"), list.items().get(1));
        assertEquals(new SNumber(new java.math.BigDecimal("1")), list.items().get(2));
        assertEquals(new SSymbol(":active"), list.items().get(3));
        assertEquals(new SBool(true), list.items().get(4));
        assertEquals(new SSymbol(":note"), list.items().get(5));
        assertEquals(new SString("hi"), list.items().get(6));
    }
}
