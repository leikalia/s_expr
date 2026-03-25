package org.example.sexpr.parse;

public final class ParseException extends RuntimeException {
    private final int pos;

    public ParseException(String message, int pos) {
        super(message + " at pos=" + pos);
        this.pos = pos;
    }

    public int pos() { return pos; }
}

