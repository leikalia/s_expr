package org.example.sexpr;

import org.example.sexpr.fluent.QueryDsl;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.Updater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StressLargeDataTest {

    private final SExprParser parser = new SExprParser();
    private final QueryEngine engine = new QueryEngine();
    private final Updater updater = new Updater();

    private String buildLargeDoc(int usersCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("(root (users ");
        for (int i = 1; i <= usersCount; i++) {
            boolean active = (i % 2 == 0); 
            sb.append("(user :id ").append(i).append(" :active ").append(active ? "true" : "false").append(")");
        }
        sb.append(") (meta))");
        return sb.toString();
    }

    @Test
    @Timeout(value = 8, unit = TimeUnit.SECONDS) 
    void largeDataSearchAndUpdate() {
        int n = 10_000;
        var text = buildLargeDoc(n);

        var doc = parser.parse(text);

        
        int expectedActive = n / 2;
        assertEquals(expectedActive, engine.find(doc, "/root//user[:active=true]").size());

        
        var q = QueryDsl.start()
                .root()
                .child("root")
                .desc("user")
                .where().attrEq(":active", true).done()
                .build();

        assertEquals(expectedActive, q.find(doc).size());

        
        var res = updater.apply(doc, "/root//user[:id=2000]", Mutation.setAttr(":active", new org.example.sexpr.ast.SBool(false)));
        assertEquals(1, res.affectedCount());

        var doc2 = res.newRoot();

        
        assertEquals(expectedActive - 1, engine.find(doc2, "/root//user[:active=true]").size());
    }
}
