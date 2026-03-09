package org.example.sexpr.parse;

public record Token(TokenType type, String text, int pos) {}

