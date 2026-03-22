package org.example.sexpr.fluent;

import java.math.BigDecimal;

public interface FilterStep {
    FilterStep attrExists(String attrName);

    FilterStep attrEq(String attrName, int value);
    FilterStep attrEq(String attrName, long value);
    FilterStep attrEq(String attrName, BigDecimal value);
    FilterStep attrEq(String attrName, boolean value);
    FilterStep attrEq(String attrName, String value);
    FilterStep attrEqNull(String attrName);

    TerminalStep done();
}
