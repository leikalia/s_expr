package org.example.sexpr.print;

import org.example.sexpr.ast.*;

import java.util.stream.Collectors;

public final class SExprPrinter {

    public String print(SNode node) {
        return switch (node) {
            case SList list -> "(" + list.items().stream().map(this::print).collect(Collectors.joining(" ")) + ")";
            case SSymbol sym -> sym.value();
            case SString str -> "\"" + escape(str.value()) + "\"";
            case SNumber num -> num.value().toPlainString();
            case SBool b -> b.value() ? "true" : "false";
            case SNull n -> "null";
        };
    }

    private String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\r' -> sb.append("\\r");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}