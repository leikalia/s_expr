package org.example.sexpr;

import org.example.sexpr.ast.SList;
import org.example.sexpr.ast.SSymbol;
import org.example.sexpr.print.SExprPrinter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SExprPrinterTest {

    @Test
    void printsSimpleList() {
        var node = new SList(List.of(new SSymbol("a"), new SSymbol("b")));
        var text = new SExprPrinter().print(node);
        assertEquals("(a b)", text);
    }
}