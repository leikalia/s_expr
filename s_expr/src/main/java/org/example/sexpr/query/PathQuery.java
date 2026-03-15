package org.example.sexpr.query;

import java.util.List;

public record PathQuery(boolean absolute, List<Step> steps) {
}
