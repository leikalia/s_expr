package org.example.sexpr;

import org.example.sexpr.parse.SExprTokenizer;
import org.example.sexpr.parse.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SExprTokenizerTest {

    @Test
    void tokenizesSimpleExpr() {
        var tokens = new SExprTokenizer().tokenize("(user :id 1 :active true)");
        assertEquals(TokenType.LPAREN, tokens.get(0).type());
        assertEquals("user", tokens.get(1).text());
        assertEquals(":id", tokens.get(2).text());
        assertEquals("1", tokens.get(3).text());
        assertEquals(":active", tokens.get(4).text());
        assertEquals("true", tokens.get(5).text());
        assertEquals(TokenType.RPAREN, tokens.get(6).type());
        assertEquals(TokenType.EOF, tokens.get(7).type());
    }
}

