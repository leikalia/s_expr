package org.example.sexpr.query;

import org.example.sexpr.ast.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PathParser {

    public PathQuery parse(String path) {
        if (path == null) throw new IllegalArgumentException("path is null");
        if (path.isBlank()) throw new IllegalArgumentException("path is blank");

        int i = 0;
        boolean absolute = path.startsWith("/");

        List<Step> steps = new ArrayList<>();

        while (i < path.length()) {
            if (path.charAt(i) != '/') {
                // относительный путь: первый шаг без ведущего "/"
                ParsedStep ps = readStep(path, i, Axis.CHILD);
                steps.add(ps.step);
                i = ps.nextIndex;
                continue;
            }

            // '/'
            if (i + 1 < path.length() && path.charAt(i + 1) == '/') {
                i += 2; // пропускаем "//"
                ParsedStep ps = readStep(path, i, Axis.DESCENDANT);
                steps.add(ps.step);
                i = ps.nextIndex;
            } else {
                i += 1; // пропускаем "/"
                if (i >= path.length()) break; // "/" в конце => это запрос на корень
                ParsedStep ps = readStep(path, i, Axis.CHILD);
                steps.add(ps.step);
                i = ps.nextIndex;
            }
        }

        if (steps.isEmpty()) {
            // путь "/" -> корневой узел
            return new PathQuery(true, List.of());
        }

        return new PathQuery(absolute, List.copyOf(steps));
    }

    private ParsedStep readStep(String path, int start, Axis axis) {
        String name = readName(path, start);
        int i = start + name.length();

        List<Predicate> predicates = new ArrayList<>();

        while (i < path.length() && path.charAt(i) == '[') {
            int close = findClosingBracket(path, i);
            String inside = path.substring(i + 1, close).trim();
            if (inside.isEmpty()) {
                throw new IllegalArgumentException("Empty predicate [] in path: " + path);
            }

            predicates.add(parsePredicate(inside, path));
            i = close + 1;
        }

        return new ParsedStep(new Step(axis, name, predicates), i);
    }

    private String readName(String path, int start) {
        if (start >= path.length()) throw new IllegalArgumentException("Expected name at end of path: " + path);

        int i = start;
        while (i < path.length()) {
            char c = path.charAt(i);
            if (c == '/' || c == '[') break;
            i++;
        }

        String name = path.substring(start, i).trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Empty step in path: " + path);
        return name;
    }

    private int findClosingBracket(String path, int openIndex) {
        int i = openIndex + 1;
        boolean inString = false;

        while (i < path.length()) {
            char c = path.charAt(i);

            if (c == '"' && (i == openIndex + 1 || path.charAt(i - 1) != '\\')) {
                inString = !inString;
                i++;
                continue;
            }

            if (!inString && c == ']') return i;
            i++;
        }

        throw new IllegalArgumentException("Unclosed predicate '[' in path: " + path);
    }

    private Predicate parsePredicate(String inside, String fullPath) {
        String s = inside.trim();

        int eq = indexOfTopLevelEquals(s);
        if (eq < 0) {
            String attr = s.trim();
            if (!attr.startsWith(":") || attr.length() == 1) {
                throw new IllegalArgumentException("Predicate must start with attribute like :id in path: " + fullPath);
            }
            return new AttrExistsPredicate(attr);
        }

        String left = s.substring(0, eq).trim();
        String right = s.substring(eq + 1).trim();

        if (!left.startsWith(":") || left.length() == 1) {
            throw new IllegalArgumentException("Left side must be attribute like :id in path: " + fullPath);
        }
        if (right.isEmpty()) {
            throw new IllegalArgumentException("Right side of '=' is empty in path: " + fullPath);
        }

        SNode value = parseValue(right, fullPath);
        return new AttrEqualsPredicate(left, value);
    }

    private int indexOfTopLevelEquals(String s) {
        // '=' внутри строки игнорируем
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }
            if (!inString && c == '=') return i;
        }
        return -1;
    }

    private SNode parseValue(String raw, String fullPath) {
        if (raw.startsWith("\"")) {
            return new SString(parseQuotedString(raw, fullPath));
        }
        if (raw.equals("true")) return new SBool(true);
        if (raw.equals("false")) return new SBool(false);
        if (raw.equals("null")) return SNull.INSTANCE;

        if (looksLikeNumber(raw)) {
            try {
                return new SNumber(new BigDecimal(raw));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad number in predicate: " + raw + " in path: " + fullPath);
            }
        }

        // иначе считаем символом (например admin)
        return new SSymbol(raw);
    }

    private String parseQuotedString(String raw, String fullPath) {
        // ожидаем одну строку в кавычках без хвоста
        if (raw.length() < 2 || raw.charAt(0) != '"') {
            throw new IllegalArgumentException("Bad string literal in path: " + fullPath);
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c == '"') {
                if (i != raw.length() - 1) {
                    throw new IllegalArgumentException("Extra chars after string literal in path: " + fullPath);
                }
                return sb.toString();
            }
            if (c == '\\') {
                if (i + 1 >= raw.length()) {
                    throw new IllegalArgumentException("Unterminated escape in string predicate in path: " + fullPath);
                }
                char next = raw.charAt(i + 1);
                switch (next) {
                    case '\\' -> sb.append('\\');
                    case '"' -> sb.append('"');
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    default -> sb.append(next);
                }
                i += 2;
                continue;
            }
            sb.append(c);
            i++;
        }
        throw new IllegalArgumentException("Unclosed string literal in predicate in path: " + fullPath);
    }

    private boolean looksLikeNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        if (s.charAt(0) == '-') { if (s.length() == 1) return false; i = 1; }
        boolean hasDigits = false, hasDot = false;

        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') { if (hasDot) return false; hasDot = true; continue; }
            if (!Character.isDigit(c)) return false;
            hasDigits = true;
        }
        return hasDigits;
    }

    private record ParsedStep(Step step, int nextIndex) {}
}

