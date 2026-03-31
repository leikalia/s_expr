package org.example.sexpr.parse;

import java.util.ArrayList;
import java.util.List;

public final class SExprTokenizer {

    public List<Token> tokenize(String input) {
        if (input == null) throw new IllegalArgumentException("input is null");

        List<Token> out = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) { i++; continue; }

            if (c == ';') {
                i++;
                while (i < input.length() && input.charAt(i) != '\n') i++;
                continue;
            }

            if (c == '(') { out.add(new Token(TokenType.LPAREN, "(", i)); i++; continue; }
            if (c == ')') { out.add(new Token(TokenType.RPAREN, ")", i)); i++; continue; }

            if (c == '"') {
                int start = i;
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < input.length()) {
                    char ch = input.charAt(i);
                    if (ch == '"') { i++; break; }
                    if (ch == '\\') {
                        if (i + 1 >= input.length())
                            throw new ParseException("Unterminated escape sequence", start);
                        char next = input.charAt(i + 1);
                        switch (next) {
                            case '\\' -> sb.append('\\');
                            case '"' -> sb.append('"');
                            case 'n' -> sb.append('\n');
                            case 't' -> sb.append('\t');
                            case 'r' -> sb.append('\r');
                            default -> sb.append(next);
                        }
                        i += 2;
                        continue;
                    }
                    sb.append(ch);
                    i++;
                }
                out.add(new Token(TokenType.STRING, sb.toString(), start));
                continue;
            }

            int start = i;
            while (i < input.length()) {
                char ch = input.charAt(i);
                if (Character.isWhitespace(ch) || ch == '(' || ch == ')' || ch == ';') break;
                i++;
            }
            String text = input.substring(start, i);

            if (looksLikeNumber(text)) out.add(new Token(TokenType.NUMBER, text, start));
            else out.add(new Token(TokenType.SYMBOL, text, start));
        }

        out.add(new Token(TokenType.EOF, "", input.length()));
        return out;
    }

    private boolean looksLikeNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        if (s.charAt(0) == '-') { if (s.length() == 1) return false; i = 1; }
        boolean hasDigits = false, hasDot = false;

        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') { if (hasDot) return false; hasDot = true; continue; }
            if (!Character.isDigit(c)) return false;
            hasDigits = true;
        }
        return hasDigits;
    }
}

