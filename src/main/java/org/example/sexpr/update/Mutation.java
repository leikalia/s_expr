package org.example.sexpr.update;

import org.example.sexpr.ast.SList;
import org.example.sexpr.ast.SNode;

import java.util.Objects;

public sealed interface Mutation permits Mutation.SetAttr, Mutation.RemoveAttr, Mutation.ReplaceWith, Mutation.Delete {

    record SetAttr(String attrName, SNode value) implements Mutation {
        public SetAttr {
            Objects.requireNonNull(attrName);
            Objects.requireNonNull(value);
        }
    }

    record RemoveAttr(String attrName) implements Mutation {
        public RemoveAttr {
            Objects.requireNonNull(attrName);
        }
    }

    record ReplaceWith(SList newNode) implements Mutation {
        public ReplaceWith {
            Objects.requireNonNull(newNode);
        }
    }

    record Delete() implements Mutation {
        public static final Delete INSTANCE = new Delete();
    }

    static SetAttr setAttr(String attrName, SNode value) {
        return new SetAttr(attrName, value);
    }

    static RemoveAttr removeAttr(String attrName) {
        return new RemoveAttr(attrName);
    }

    static ReplaceWith replaceWith(SList newNode) {
        return new ReplaceWith(newNode);
    }

    static Delete delete() {
        return Delete.INSTANCE;
    }
}
