package org.example.sexpr;

import org.example.sexpr.ast.SNode;
import org.example.sexpr.fluent.QueryDsl;
import org.example.sexpr.fluent.StartStep;
import org.example.sexpr.parse.SExprParser;
import org.example.sexpr.print.SExprPrinter;
import org.example.sexpr.query.Match;
import org.example.sexpr.query.QueryEngine;
import org.example.sexpr.schema.Schema;
import org.example.sexpr.schema.SchemaParser;
import org.example.sexpr.schema.ValidationResult;
import org.example.sexpr.schema.Validator;
import org.example.sexpr.update.Mutation;
import org.example.sexpr.update.UpdateResult;
import org.example.sexpr.update.Updater;

import java.util.List;

public final class SExpr {

    private SExpr() {}

    /**
     * Разбирает строку с одним S-выражением и возвращает AST-узел.
     *
     * @param input текст S-выражения, например {@code "(root (child :k v))"}
     * @return корневой узел AST
     * @throws org.example.sexpr.parse.ParseException если входная строка некорректна
     */
    public static SNode parse(String input) {
        return new SExprParser().parse(input);
    }

    /**
     * Сериализует AST-узел обратно в строку.
     *
     * @param node узел для печати
     * @return каноническое строковое представление
     */
    public static String print(SNode node) {
        return new SExprPrinter().print(node);
    }

    /**
     * Выполняет XPath-подобный запрос по дереву.
     *
     * <p>Примеры путей:
     * <ul>
     *   <li>{@code "/root/users/user"}      — абсолютный путь, прямые потомки</li>
     *   <li>{@code "//user"}                — все элементы {@code user} в дереве</li>
     *   <li>{@code "//user[:id=42]"}        — фильтр по атрибуту</li>
     *   <li>{@code "//user[:active=true]"}  — булев фильтр</li>
     * </ul>
     *
     * @param root документ или поддерево для поиска
     * @param path выражение пути
     * @return список совпадений (путь + узел); пустой список, если ничего не найдено
     */
    public static List<Match> find(SNode root, String path) {
        return new QueryEngine().find(root, path);
    }

    /**
     * Применяет мутацию ко всем узлам, удовлетворяющим запросу.
     *
     * <p>Исходное дерево не изменяется — возвращается новый корень.
     * Поведение аналогично иммутабельным обновлениям в функциональном
     * программировании: «копируй при записи» вверх по пути.
     *
     * @param root     исходный корень документа
     * @param path     путь для выбора целевых узлов
     * @param mutation мутация: {@link Mutation#setAttr}, {@link Mutation#removeAttr},
     *                 {@link Mutation#replaceWith} или {@link Mutation#delete}
     * @return результат с новым деревом и счётчиком изменённых узлов
     */
    public static UpdateResult update(SNode root, String path, Mutation mutation) {
        return new Updater().apply(root, path, mutation);
    }


    /**
     * Возвращает стартовый шаг fluent-построителя запросов.
     *
     * <p>Аналог записи через точку вместо строки-пути — удобен, когда запрос
     * строится динамически:
     * <pre>{@code
     * SExpr.query()
     *      .root().child("root").desc("user")
     *      .where().attrEq(":role", "admin").done()
     *      .build()
     *      .find(doc);
     * }</pre>
     *
     * @return начальный шаг DSL ({@link StartStep})
     */
    public static StartStep query() {
        return QueryDsl.start();
    }


    /**
     * Разбирает AST схемы в объект {@link Schema}.
     *
     * <p>Схема задаётся как S-выражение вида:
     * <pre>{@code
     * (schema
     *   (root myRoot)
     *   (element myRoot (attrs) (children (child 1 N)))
     *   (element child  (attrs (:id number required)) (children))
     * )
     * }</pre>
     *
     * @param schemaAst узел, возвращённый {@link #parse} из текста схемы
     * @return скомпилированная схема
     */
    public static Schema parseSchema(SNode schemaAst) {
        return new SchemaParser().parse(schemaAst);
    }

    /**
     * Проверяет документ на соответствие схеме.
     *
     * @param doc    корень документа
     * @param schema схема, полученная из {@link #parseSchema}
     * @return результат валидации: {@code ok()} и список нарушений
     */
    public static ValidationResult validate(SNode doc, Schema schema) {
        return new Validator().validate(doc, schema);
    }


    /**
     * Выполняет полный цикл parse → print для нормализации текста.
     *
     * <p>Удобно для приведения произвольно отформатированных S-выражений
     * к каноническому виду перед сравнением или сохранением.
     *
     * @param input произвольно отформатированное S-выражение
     * @return нормализованная строка
     */
    public static String normalize(String input) {
        return print(parse(input));
    }
}
