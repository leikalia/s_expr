package org.example.sexpr.fluent;

public interface StartStep {
    PathStep root(); // absolute XPath
    PathStep here(); // relative XPath
}
