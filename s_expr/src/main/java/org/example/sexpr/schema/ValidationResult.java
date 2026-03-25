package org.example.sexpr.schema;

import java.util.List;

public record ValidationResult(boolean ok, List<Violation> violations) {
}
