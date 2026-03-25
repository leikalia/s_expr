package org.example.sexpr.parse;

import org.example.sexpr.ast.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class SExprParser {

    public SNode parse(String input) {
        var tokens = new SExprTokenizer().tokenize(input);
        var c = new Cursor(tokens);

        SNode node = parseNode(c);

        Token t = c.peek();
        if (t.type() != TokenType.EOF) {
            throw new ParseException("Trailing tokens after first expression: " + t.text(), t.pos());
        }

        return node;
    }

    private SNode parseNode(Cursor c) {
        Token t = c.peek();

        return switch (t.type()) {
            case LPAREN -> parseList(c);
            case STRING -> {
                c.next();
                yield new SString(t.text());
            }
            case NUMBER -> {
                c.next();
                try {
                    yield new SNumber(new BigDecimal(t.text()));
                } catch (NumberFormatException ex) {
                    throw new ParseException("Bad number: " + t.text(), t.pos());
                }
            }
            case SYMBOL -> {
                c.next();
                yield parseSymbolAtom(t);
            }
            case RPAREN -> throw new ParseException("Unexpected ')'", t.pos());
            case EOF -> throw new ParseException("Unexpected EOF", t.pos());
        };
    }

    private SList parseList(Cursor c) {
        Token open = c.expect(TokenType.LPAREN);
        List<SNode> items = new ArrayList<>();

        while (true) {
            Token t = c.peek();
            if (t.type() == TokenType.RPAREN) {
                c.next(); // съели ')'
                break;
            }
            if (t.type() == TokenType.EOF) {
                throw new ParseException("Unclosed list", open.pos());
            }
            items.add(parseNode(c));
        }

        return new SList(items);
    }

    private SAtom parseSymbolAtom(Token t) {
        return switch (t.text()) {
            case "true" -> new SBool(true);
            case "false" -> new SBool(false);
            case "null" -> SNull.INSTANCE;
            default -> new SSymbol(t.text());
        };
    }

    private static final class Cursor {
        private final List<Token> tokens;
        private int i = 0;

        private Cursor(List<Token> tokens) {
            this.tokens = tokens;
        }

        Token peek() {
            return tokens.get(i);
        }

        Token next() {
            return tokens.get(i++);
        }

        Token expect(TokenType type) {
            Token t = peek();
            if (t.type() != type) {
                throw new ParseException("Expected " + type + " but got " + t.type(), t.pos());
            }
            return next();
        }
    }
}

