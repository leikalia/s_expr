package org.example.sexpr;

import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundTripTest {

    @Test
    void parsePrintParseGivesSameTree() {
        var parser = new SExprParser();
        var printer = new SExprPrinter();

        var text = "(user :id 1 :active true (tags \"a\" \"b\"))";

        var node1 = parser.parse(text);
        var text2 = printer.print(node1);
        var node2 = parser.parse(text2);

        assertEquals(node1, node2);
    }
}

