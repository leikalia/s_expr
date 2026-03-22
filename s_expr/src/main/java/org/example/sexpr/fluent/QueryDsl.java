package org.example.sexpr.fluent;

public final class QueryDsl {
    private QueryDsl() {}

    public static StartStep start() {
        return new Builder();
    }
}
