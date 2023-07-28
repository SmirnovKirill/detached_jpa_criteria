package kirill.detachedjpacriteria.expression.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionType;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedCaseImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedCoalesceImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedInImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedSimpleCaseImpl;
import kirill.detachedjpacriteria.expression.api.extra.DetachedCase;
import kirill.detachedjpacriteria.expression.api.extra.DetachedCoalesce;
import kirill.detachedjpacriteria.expression.api.extra.DetachedIn;
import kirill.detachedjpacriteria.expression.api.extra.DetachedSimpleCase;
import kirill.detachedjpacriteria.expression.impl.DetachedParameterExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedPathImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedPredicateImpl;
import kirill.detachedjpacriteria.query.DetachedCriteriaDeleteImpl;
import kirill.detachedjpacriteria.query.DetachedCriteriaQueryImpl;
import kirill.detachedjpacriteria.query.DetachedCriteriaSubqueryImpl;
import kirill.detachedjpacriteria.query.DetachedCriteriaUpdateImpl;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaDelete;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaSubquery;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaUpdate;

/**
 * Detached версия класса javax.persistence.criteria.CriteriaBuilder.
 * @see CriteriaBuilder
 */
public final class DetachedCriteriaBuilder {
  private DetachedCriteriaBuilder() {
  }

  // Тут начинается блок методов которых нет в оригинальном CriteriaBuilder, они добавлены нами для удобства.

  /**
   * Выражение для получения айди сущности. По сути аналог вызова path("id") только при этом не нужно знать конкретное имя атрибута с ключом.
   */
  public static <T> DetachedPath<T> id() {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.ID, (String) null);
  }

  /**
   * Выражение для получения самой сущности (корня). Аналог Root из JPA. Можно использовать например в связке с count.
   */
  public static <T> DetachedPath<T> root() {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.ROOT, (String) null);
  }

  /**
   * Выражение для получения атрибута сущности по составному пути с заходом в коллекции или другие сущности. Например, можно написать
   * path("posts", "comments", "text") чтобы получить атрибут с текстом комментария для пользователя у которого есть набор постов и в каждом посте
   * есть комментарии. Или же наоборот можно писать path("post", "user", "name") для того чтобы для комментария найти пост, юзера и получить доступ к
   * атрибуту с его именем.
   */
  public static <T> DetachedPath<T> path(String... attributeNames) {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.PATH, attributeNames);
  }

  /**
   * Выражение для получения айди родительской сущности. Метод предназначен для работы с подзапросами.
   *
   * @see DetachedCriteriaBuilder#id()
   */
  public static <T> DetachedPath<T> parentId() {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.PARENT_ID, (String) null);
  }

  /**
   * Выражение для получения родительской сущности (корня). Аналог Root из JPA. Метод предназначен для работы с подзапросами.
   *
   * @see DetachedCriteriaBuilder#root()
   */
  public static <T> DetachedPath<T> parentRoot() {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.PARENT_ROOT, (String) null);
  }

  /**
   * Выражение для получения атрибута родительской сущности по составному пути с заходом в коллекции или другие сущности. Метод предназначен для
   * работы с подзапросами.
   *
   * @see DetachedCriteriaBuilder#path(String...)
   */
  public static <T> DetachedPath<T> parentPath(String... attributeNames) {
    return new DetachedPathImpl<>(DetachedPathImpl.Mode.PARENT_PATH, attributeNames);
  }

  /**
   * Создает билдер для построения detached запроса, который выбирает только одно выражение, это аналог CriteriaQuery.select. Разница с методом
   * multiselect ровно такая же как в JPA между select и multiselect.
   * @see CriteriaQuery#select(Selection)
   */
  public static <T> DetachedCriteriaQuery.FirstStepQueryBuilderSingleWithType<T> select(DetachedExpression<T> expression) {
    return new DetachedCriteriaQueryImpl.FirstStepQueryBuilderSingleWithType<>(expression);
  }

  /**
   * Создает билдер для построения detached запроса, который выбирает только одно выражение, это аналог CriteriaQuery.select. Разница с методом
   * multiselect ровно такая же как в JPA между select и multiselect.
   * @see CriteriaQuery#select(Selection)
   */
  public static <T> DetachedCriteriaQuery.FirstStepQueryBuilderSingleWithoutType select(DetachedPath<T> pathExpression) {
    return new DetachedCriteriaQueryImpl.FirstStepQueryBuilderSingleWithoutType(pathExpression);
  }

  /**
   * Создает билдер для построения detached запроса, который выбирает одно или несколько выражений, это аналог CriteriaQuery.multiselect. Разница с
   * методом select ровно такая же как в JPA между select и multiselect.
   * @see CriteriaQuery#multiselect(Selection[])
   */
  public static <T> DetachedCriteriaQuery.FirstStepQueryBuilderMulti multiselect(List<DetachedExpression<?>> expressions) {
    return new DetachedCriteriaQueryImpl.FirstStepQueryBuilderMulti(expressions);
  }

  /**
   * Создает билдер для построения detached запроса, который выбирает одно или несколько выражений, это аналог CriteriaQuery.multiselect. Разница с
   * методом select ровно такая же как в JPA между select и multiselect.
   * @see CriteriaQuery#multiselect(Selection[])
   */
  public static <T> DetachedCriteriaQuery.FirstStepQueryBuilderMulti multiselect(DetachedExpression<?>... expressions) {
    return new DetachedCriteriaQueryImpl.FirstStepQueryBuilderMulti(Arrays.asList(expressions));
  }

  /**
   * Создает detached запрос, который делает count для переданного выражения и класса.
   */
  public static <T> DetachedCriteriaQuery<Long> selectCount(DetachedExpression<?> countExpression, Class<T> fromClass) {
    return new DetachedCriteriaQueryImpl<>(DetachedCriteriaQueryImpl.SelectMode.SINGLE, List.of(count(countExpression)), Long.class, fromClass);
  }

  /**
   * Создает detached запрос, который делает count(distinct) для переданного выражения и класса.
   */
  public static <T> DetachedCriteriaQuery<Long> selectCountDistinct(DetachedExpression<?> countExpression, Class<T> fromClass) {
    return new DetachedCriteriaQueryImpl<>(
        DetachedCriteriaQueryImpl.SelectMode.SINGLE,
        List.of(countDistinct(countExpression)),
        Long.class,
        fromClass
    );
  }

  /**
   * Создает detached запрос, который работает с переданной сущностью - будет выбираться сущность целиком из соответствующей ей таблицы.
   */
  public static <T> DetachedCriteriaQuery<T> selectEntity(Class<T> entityClass) {
    return new DetachedCriteriaQueryImpl<>(null, null, entityClass, entityClass);
  }

  /**
   * Создает билдер для построения detached подзапроса, это аналог CommonAbstractCriteria.subquery.
   * @see CommonAbstractCriteria#subquery(Class)
   */
  public static <T> DetachedCriteriaSubquery.FirstStepQueryBuilderWithType<T> subquerySelect(DetachedExpression<T> expression) {
    return new DetachedCriteriaSubqueryImpl.FirstStepQueryBuilderWithType<>(expression);
  }

  /**
   * Создает билдер для построения detached подзапроса, это аналог CommonAbstractCriteria.subquery.
   * @see CommonAbstractCriteria#subquery(Class)
   */
  public static <T> DetachedCriteriaSubquery.FirstStepQueryBuilderWithoutType subquerySelect(DetachedPath<T> pathExpression) {
    return new DetachedCriteriaSubqueryImpl.FirstStepQueryBuilderWithoutType(pathExpression);
  }

  /**
   * Создает detached подзапрос, который работает с переданной сущностью - будет выбираться сущность целиком из соответствующей ей таблицы.
   */
  public static <T> DetachedCriteriaSubquery<T> subquerySelectEntity(Class<T> entityClass) {
    return new DetachedCriteriaSubqueryImpl<>(root(), entityClass, entityClass);
  }

  /**
   * @see CriteriaBuilder#createCriteriaUpdate(Class)
   */
  public static <T> DetachedCriteriaUpdate<T> update(Class<T> entityClass) {
    return new DetachedCriteriaUpdateImpl<>(entityClass);
  }

  /**
   * @see CriteriaBuilder#createCriteriaDelete(Class)
   */
  public static <T> DetachedCriteriaDelete<T> delete(Class<T> entityClass) {
    return new DetachedCriteriaDeleteImpl<>(entityClass);
  }

  // Тут заканчивается блок методов которых нет в оригинальном CriteriaBuilder.


  /**
   * @see CriteriaBuilder#avg(Expression)
   */
  public static <N extends Number> DetachedExpression<Double> avg(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.AVG, List.of(x));
  }

  /**
   * @see CriteriaBuilder#sum(Expression)
   */
  public static <N extends Number> DetachedExpression<N> sum(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM, List.of(x));
  }

  /**
   * @see CriteriaBuilder#sumAsLong(Expression)
   */
  public static DetachedExpression<Long> sumAsLong(DetachedExpression<Integer> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM_AS_LONG, List.of(x));
  }

  /**
   * @see CriteriaBuilder#sumAsDouble(Expression)
   */
  public static DetachedExpression<Double> sumAsDouble(DetachedExpression<Float> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM_AS_DOUBLE, List.of(x));
  }

  /**
   * @see CriteriaBuilder#max(Expression)
   */
  public static <N extends Number> DetachedExpression<N> max(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.MAX, List.of(x));
  }

  /**
   * @see CriteriaBuilder#min(Expression) 
   */
  public static <N extends Number> DetachedExpression<N> min(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.MIN, List.of(x));
  }

  /**
   * @see CriteriaBuilder#greatest(Expression) 
   */
  public static <X extends Comparable<? super X>> DetachedExpression<X> greatest(DetachedExpression<X> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.GREATEST, List.of(x));
  }

  /**
   * @see CriteriaBuilder#least(Expression) 
   */
  public static <X extends Comparable<? super X>> DetachedExpression<X> least(DetachedExpression<X> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LEAST, List.of(x));
  }

  /**
   * @see CriteriaBuilder#count(Expression) 
   */
  public static DetachedExpression<Long> count(DetachedExpression<?> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.COUNT, List.of(x));
  }

  /**
   * @see CriteriaBuilder#countDistinct(Expression) 
   */
  public static DetachedExpression<Long> countDistinct(DetachedExpression<?> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.COUNT_DISTINCT, List.of(x));
  }

  /**
   * @see CriteriaBuilder#exists(Subquery)
   */ 
  public static DetachedPredicate exists(DetachedCriteriaSubquery<?> subquery) {
    return new DetachedPredicateImpl(DetachedExpressionType.EXISTS, List.of(subquery));
  }

  /**
   * @see CriteriaBuilder#all(Subquery) 
   */
  public static <Y> DetachedExpression<Y> all(DetachedCriteriaSubquery<Y> subquery) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.ALL, List.of(subquery));
  }

  /**
   * @see CriteriaBuilder#some(Subquery) 
   */
  public static <Y> DetachedExpression<Y> some(DetachedCriteriaSubquery<Y> subquery) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SOME, List.of(subquery));
  }

  /**
   * @see CriteriaBuilder#any(Subquery) 
   */
  public static <Y> DetachedExpression<Y> any(DetachedCriteriaSubquery<Y> subquery) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.ANY, List.of(subquery));
  }

  /**
   * @see CriteriaBuilder#and(Expression, Expression) 
   */
  public static DetachedPredicate and(DetachedExpression<Boolean> x, DetachedExpression<Boolean> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.AND, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#and(Predicate...) 
   */
  public static DetachedPredicate and(DetachedPredicate... restrictions) {
    return new DetachedPredicateImpl(DetachedExpressionType.AND, Arrays.asList(restrictions));
  }

  /**
   * @see CriteriaBuilder#or(Expression, Expression) 
   */
  public static DetachedPredicate or(DetachedExpression<Boolean> x, DetachedExpression<Boolean> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.OR, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#or(Predicate...) 
   */
  public static DetachedPredicate or(DetachedPredicate... restrictions) {
    return new DetachedPredicateImpl(DetachedExpressionType.OR, Arrays.asList(restrictions));
  }

  /**
   * @see CriteriaBuilder#not(Expression) 
   */
  public static DetachedPredicate not(DetachedExpression<Boolean> restriction) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT, List.of(restriction));
  }

  /**
   * @see CriteriaBuilder#isTrue(Expression) 
   */
  public static DetachedPredicate isTrue(DetachedExpression<Boolean> x) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_TRUE, List.of(x));
  }

  /**
   * @see CriteriaBuilder#isFalse(Expression) 
   */
  public static DetachedPredicate isFalse(DetachedExpression<Boolean> x) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_FALSE, List.of(x));
  }

  /**
   * @see CriteriaBuilder#isNull(Expression) 
   */
  public static DetachedPredicate isNull(DetachedExpression<?> x) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_NULL, List.of(x));
  }

  /**
   * @see CriteriaBuilder#isNotNull(Expression) 
   */
  public static DetachedPredicate isNotNull(DetachedExpression<?> x) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_NOT_NULL, List.of(x));
  }

  /**
   * @see CriteriaBuilder#equal(Expression, Expression) 
   */
  public static DetachedPredicate equal(DetachedExpression<?> x, DetachedExpression<?> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.EQUAL, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#equal(Expression, Object) 
   */
  public static DetachedPredicate equal(DetachedExpression<?> x, Object y) {
    return new DetachedPredicateImpl(DetachedExpressionType.EQUAL, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#notEqual(Expression, Expression) 
   */
  public static DetachedPredicate notEqual(DetachedExpression<?> x, DetachedExpression<?> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_EQUAL, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#notEqual(Expression, Object) 
   */
  public static DetachedPredicate notEqual(DetachedExpression<?> x, Object y) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_EQUAL, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#greaterThan(Expression, Expression) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate greaterThan(
      DetachedExpression<? extends Y> x,
      DetachedExpression<? extends Y> y
  ) {
    return new DetachedPredicateImpl(DetachedExpressionType.GREATER_THAN, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#greaterThan(Expression, Comparable) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate greaterThan(DetachedExpression<? extends Y> x, Y y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GREATER_THAN, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#greaterThanOrEqualTo(Expression, Expression) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate greaterThanOrEqualTo(
      DetachedExpression<? extends Y> x,
      DetachedExpression<? extends Y> y
  ) {
    return new DetachedPredicateImpl(DetachedExpressionType.GREATER_THAN_OR_EQUAL_TO, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#greaterThanOrEqualTo(Expression, Comparable) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate greaterThanOrEqualTo(DetachedExpression<? extends Y> x, Y y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GREATER_THAN_OR_EQUAL_TO, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lessThan(Expression, Expression) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate lessThan(DetachedExpression<? extends Y> x, DetachedExpression<? extends Y> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LESS_THAN, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lessThan(Expression, Comparable) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate lessThan(DetachedExpression<? extends Y> x, Y y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LESS_THAN, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lessThanOrEqualTo(Expression, Expression) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate lessThanOrEqualTo(
      DetachedExpression<? extends Y> x,
      DetachedExpression<? extends Y> y
  ) {
    return new DetachedPredicateImpl(DetachedExpressionType.LESS_THAN_OR_EQUAL_TO, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lessThanOrEqualTo(Expression, Comparable) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate lessThanOrEqualTo(DetachedExpression<? extends Y> x, Y y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LESS_THAN_OR_EQUAL_TO, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#between(Expression, Expression, Expression) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate between(
      DetachedExpression<? extends Y> v,
      DetachedExpression<? extends Y> x,
      DetachedExpression<? extends Y> y
  ) {
    return new DetachedPredicateImpl(DetachedExpressionType.BETWEEN, List.of(v, x, y));
  }

  /**
   * @see CriteriaBuilder#between(Expression, Comparable, Comparable) 
   */
  public static <Y extends Comparable<? super Y>> DetachedPredicate between(DetachedExpression<? extends Y> v, Y x, Y y) {
    return new DetachedPredicateImpl(DetachedExpressionType.BETWEEN, List.of(v, x, y));
  }

  /**
   * @see CriteriaBuilder#gt(Expression, Expression) 
   */
  public static DetachedPredicate gt(DetachedExpression<? extends Number> x, DetachedExpression<? extends Number> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#gt(Expression, Number) 
   */
  public static DetachedPredicate gt(DetachedExpression<? extends Number> x, Number y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#ge(Expression, Expression) 
   */
  public static DetachedPredicate ge(DetachedExpression<? extends Number> x, DetachedExpression<? extends Number> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#ge(Expression, Number) 
   */
  public static DetachedPredicate ge(DetachedExpression<? extends Number> x, Number y) {
    return new DetachedPredicateImpl(DetachedExpressionType.GE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lt(Expression, Expression) 
   */
  public static DetachedPredicate lt(DetachedExpression<? extends Number> x, DetachedExpression<? extends Number> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#lt(Expression, Number) 
   */
  public static DetachedPredicate lt(DetachedExpression<? extends Number> x, Number y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#le(Expression, Expression) 
   */
  public static DetachedPredicate le(DetachedExpression<? extends Number> x, DetachedExpression<? extends Number> y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#le(Expression, Number) 
   */
  public static DetachedPredicate le(DetachedExpression<? extends Number> x, Number y) {
    return new DetachedPredicateImpl(DetachedExpressionType.LE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#neg(Expression) 
   */
  public static <N extends Number> DetachedExpression<N> neg(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.NEG, List.of(x));
  }

  /**
   * @see CriteriaBuilder#abs 
   */
  public static <N extends Number> DetachedExpression<N> abs(DetachedExpression<N> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.ABS, List.of(x));
  }

  /**
   * @see CriteriaBuilder#sum(Expression, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> sum(DetachedExpression<? extends N> x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#sum(Expression, Number) 
   */
  public static <N extends Number> DetachedExpression<N> sum(DetachedExpression<? extends N> x, N y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#sum(Number, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> sum(N x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUM, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#prod(Expression, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> prod(DetachedExpression<? extends N> x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.PROD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#prod(Expression, Number) 
   */
  public static <N extends Number> DetachedExpression<N> prod(DetachedExpression<? extends N> x, N y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.PROD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#prod(Number, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> prod(N x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.PROD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#diff(Expression, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> diff(DetachedExpression<? extends N> x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.DIFF, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#diff(Expression, Number) 
   */
  public static <N extends Number> DetachedExpression<N> diff(DetachedExpression<? extends N> x, N y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.DIFF, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#diff(Number, Expression) 
   */
  public static <N extends Number> DetachedExpression<N> diff(N x, DetachedExpression<? extends N> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.DIFF, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#quot(Expression, Expression) 
   */
  public static DetachedExpression<Number> quot(DetachedExpression<? extends Number> x, DetachedExpression<? extends Number> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.QUOT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#quot(Expression, Number) 
   */
  public static DetachedExpression<Number> quot(DetachedExpression<? extends Number> x, Number y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.QUOT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#quot(Number, Expression) 
   */
  public static DetachedExpression<Number> quot(Number x, DetachedExpression<? extends Number> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.QUOT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#mod(Expression, Expression) 
   */
  public static DetachedExpression<Integer> mod(DetachedExpression<Integer> x, DetachedExpression<Integer> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.MOD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#mod(Expression, Integer) 
   */
  public static DetachedExpression<Integer> mod(DetachedExpression<Integer> x, Integer y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.MOD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#mod(Integer, Expression) 
   */
  public static DetachedExpression<Integer> mod(Integer x, DetachedExpression<Integer> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.MOD, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#sqrt(Expression) 
   */
  public static DetachedExpression<Double> sqrt(DetachedExpression<? extends Number> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SQRT, List.of(x));
  }

  /**
   * @see CriteriaBuilder#toLong(Expression) 
   */
  public static DetachedExpression<Long> toLong(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_LONG, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toInteger(Expression) 
   */
  public static DetachedExpression<Integer> toInteger(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_INTEGER, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toFloat(Expression) 
   */
  public static DetachedExpression<Float> toFloat(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_FLOAT, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toDouble(Expression) 
   */
  public static DetachedExpression<Double> toDouble(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_DOUBLE, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toBigDecimal(Expression) 
   */
  public static DetachedExpression<BigDecimal> toBigDecimal(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_BIG_DECIMAL, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toBigInteger(Expression) 
   */
  public static DetachedExpression<BigInteger> toBigInteger(DetachedExpression<? extends Number> number) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_BIG_INTEGER, List.of(number));
  }

  /**
   * @see CriteriaBuilder#toString(Expression) 
   */
  public static DetachedExpression<String> toString(DetachedExpression<Character> character) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TO_STRING, List.of(character));
  }

  /**
   * @see CriteriaBuilder#literal(Object) 
   */
  public static <T> DetachedExpression<T> literal(T value) {
    Objects.requireNonNull(value);
    return new DetachedExpressionImpl<>(DetachedExpressionType.LITERAL, List.of(value));
  }

  /**
   * @see CriteriaBuilder#nullLiteral(Class) 
   */
  public static <T> DetachedExpression<T> nullLiteral(Class<T> resultClass) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.NULL_LITERAL, List.of(resultClass));
  }

  /**
   * @see CriteriaBuilder#parameter(Class, String) 
   */
  public static <T> DetachedParameterExpression<T> parameter(String name) {
    return new DetachedParameterExpressionImpl<>(name);
  }

  /**
   * @see CriteriaBuilder#isEmpty(Expression) 
   */
  public static <C extends Collection<?>> DetachedPredicate isEmpty(DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_EMPTY, List.of(collection));
  }

  /**
   * @see CriteriaBuilder#isNotEmpty(Expression) 
   */
  public static <C extends Collection<?>> DetachedPredicate isNotEmpty(DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_NOT_EMPTY, List.of(collection));
  }

  /**
   * @see CriteriaBuilder#size(Expression) 
   */
  public static <C extends java.util.Collection<?>> DetachedExpression<Integer> size(DetachedExpression<C> collection) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SIZE, List.of(collection));
  }

  /**
   * @see CriteriaBuilder#size(Collection) 
   */
  public static <C extends Collection<?>> DetachedExpression<Integer> size(C collection) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SIZE, List.of(collection));
  }

  /**
   * @see CriteriaBuilder#isMember(Expression, Expression) 
   */
  public static <E, C extends Collection<E>> DetachedPredicate isMember(DetachedExpression<E> elem, DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_MEMBER, List.of(elem, collection));
  }

  /**
   * @see CriteriaBuilder#isMember(Object, Expression) 
   */
  public static <E, C extends Collection<E>> DetachedPredicate isMember(E elem, DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_MEMBER, List.of(elem, collection));
  }

  /**
   * @see CriteriaBuilder#isNotMember(Expression, Expression) 
   */
  public static <E, C extends Collection<E>> DetachedPredicate isNotMember(DetachedExpression<E> elem, DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_NOT_MEMBER, List.of(elem, collection));
  }

  /**
   * @see CriteriaBuilder#isNotMember(Object, Expression) 
   */
  public static <E, C extends Collection<E>> DetachedPredicate isNotMember(E elem, DetachedExpression<C> collection) {
    return new DetachedPredicateImpl(DetachedExpressionType.IS_NOT_MEMBER, List.of(elem, collection));
  }

  /**
   * @see CriteriaBuilder#values(Map) 
   */
  public static <V, M extends Map<?, V>> DetachedExpression<Collection<V>> values(M map) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.VALUES, List.of(map));
  }

  /**
   * @see CriteriaBuilder#keys(Map) 
   */
  public static <K, M extends Map<K, ?>> DetachedExpression<Set<K>> keys(M map) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.KEYS, List.of(map));
  }

  /**
   * @see CriteriaBuilder#like(Expression, Expression) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, DetachedExpression<String> pattern) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#like(Expression, String) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, String pattern) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#like(Expression, Expression, Expression) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, DetachedExpression<String> pattern, DetachedExpression<Character> escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#like(Expression, Expression, char) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, DetachedExpression<String> pattern, char escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#like(Expression, String, Expression) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, String pattern, DetachedExpression<Character> escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#like(Expression, String, char) 
   */
  public static DetachedPredicate like(DetachedExpression<String> x, String pattern, char escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, Expression) 
   */
  public static DetachedPredicate notLike(DetachedExpression<String> x, DetachedExpression<String> pattern) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, String) 
   */
  public static DetachedPredicate notLike(DetachedExpression<String> x, String pattern) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, Expression, Expression) 
   */
  public static DetachedPredicate notLike(
      DetachedExpression<String> x,
      DetachedExpression<String> pattern,
      DetachedExpression<Character> escapeChar
  ) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, Expression, char) 
   */
  public static DetachedPredicate notLike(DetachedExpression<String> x, DetachedExpression<String> pattern, char escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, String, Expression) 
   */
  public static DetachedPredicate notLike(DetachedExpression<String> x, String pattern, DetachedExpression<Character> escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#notLike(Expression, String, char) 
   */
  public static DetachedPredicate notLike(DetachedExpression<String> x, String pattern, char escapeChar) {
    return new DetachedPredicateImpl(DetachedExpressionType.NOT_LIKE, List.of(x, pattern, escapeChar));
  }

  /**
   * @see CriteriaBuilder#concat(Expression, Expression) 
   */
  public static DetachedExpression<String> concat(DetachedExpression<String> x, DetachedExpression<String> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CONCAT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#concat(Expression, String) 
   */
  public static DetachedExpression<String> concat(DetachedExpression<String> x, String y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CONCAT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#concat(String, Expression) 
   */
  public static DetachedExpression<String> concat(String x, DetachedExpression<String> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CONCAT, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#substring(Expression, Expression) 
   */
  public static DetachedExpression<String> substring(DetachedExpression<String> x, DetachedExpression<Integer> from) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUBSTRING, List.of(x, from));
  }

  /**
   * @see CriteriaBuilder#substring(Expression, int) 
   */
  public static DetachedExpression<String> substring(DetachedExpression<String> x, int from) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUBSTRING, List.of(x, from));
  }

  /**
   * @see CriteriaBuilder#substring(Expression, Expression, Expression) 
   */
  public static DetachedExpression<String> substring(
      DetachedExpression<String> x,
      DetachedExpression<Integer> from,
      DetachedExpression<Integer> len
  ) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUBSTRING, List.of(x, from, len));
  }

  /**
   * @see CriteriaBuilder#substring(Expression, int, int) 
   */
  public static DetachedExpression<String> substring(DetachedExpression<String> x, int from, int len) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.SUBSTRING, List.of(x, from, len));
  }

  /**
   * @see CriteriaBuilder#trim(Expression) 
   */
  public static DetachedExpression<String> trim(DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(x));
  }

  /**
   * @see CriteriaBuilder#trim(CriteriaBuilder.Trimspec, Expression) 
   */
  public static DetachedExpression<String> trim(CriteriaBuilder.Trimspec ts, DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(ts, x));
  }

  /**
   * @see CriteriaBuilder#trim(Expression, Expression) 
   */
  public static DetachedExpression<String> trim(DetachedExpression<Character> t, DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(t));
  }

  /**
   * @see CriteriaBuilder#trim(CriteriaBuilder.Trimspec, Expression, Expression) 
   */
  public static DetachedExpression<String> trim(CriteriaBuilder.Trimspec ts, DetachedExpression<Character> t, DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(ts, t, x));
  }

  /**
   * @see CriteriaBuilder#trim(char, Expression) 
   */
  public static DetachedExpression<String> trim(char t, DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(t, x));
  }

  /**
   * @see CriteriaBuilder#trim(CriteriaBuilder.Trimspec, char, Expression)
   */
  public static DetachedExpression<String> trim(CriteriaBuilder.Trimspec ts, char t, DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TRIM, List.of(ts, t, x));
  }

  /**
   * @see CriteriaBuilder#lower(Expression) 
   */
  public static DetachedExpression<String> lower(DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LOWER, List.of(x));
  }

  /**
   * @see CriteriaBuilder#upper(Expression) 
   */
  public static DetachedExpression<String> upper(DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.UPPER, List.of(x));
  }

  /**
   * @see CriteriaBuilder#length(Expression) 
   */
  public static DetachedExpression<Integer> length(DetachedExpression<String> x) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LENGTH, List.of(x));
  }

  /**
   * @see CriteriaBuilder#locate(Expression, Expression) 
   */
  public static DetachedExpression<Integer> locate(DetachedExpression<String> x, DetachedExpression<String> pattern) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LOCATE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#locate(Expression, String)  
   */
  public static DetachedExpression<Integer> locate(DetachedExpression<String> x, String pattern) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LOCATE, List.of(x, pattern));
  }

  /**
   * @see CriteriaBuilder#locate(Expression, Expression, Expression) 
   */
  public static DetachedExpression<Integer> locate(
      DetachedExpression<String> x,
      DetachedExpression<String> pattern,
      DetachedExpression<Integer> from
  ) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LOCATE, List.of(x, pattern, from));
  }

  /**
   * @see CriteriaBuilder#locate(Expression, String, int) 
   */
  public static DetachedExpression<Integer> locate(DetachedExpression<String> x, String pattern, int from) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.LOCATE, List.of(x, pattern, from));
  }

  /**
   * @see CriteriaBuilder#currentDate() 
   */
  public static DetachedExpression<Date> currentDate() {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CURRENT_DATE, List.of());
  }

  /**
   * @see CriteriaBuilder#currentTimestamp() 
   */
  public static DetachedExpression<Timestamp> currentTimestamp() {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CURRENT_TIMESTAMP, List.of());
  }

  /**
   * @see CriteriaBuilder#currentTime() 
   */
  public static DetachedExpression<Time> currentTime() {
    return new DetachedExpressionImpl<>(DetachedExpressionType.CURRENT_TIME, List.of());
  }

  /**
   * @see CriteriaBuilder#in(Expression) 
   */
  public static <T> DetachedIn<T> in(DetachedExpression<? extends T> expression) {
    return new DetachedInImpl<>(expression);
  }

  /**
   * @see CriteriaBuilder#coalesce(Expression, Expression) 
   */
  public static <Y> DetachedExpression<Y> coalesce(DetachedExpression<? extends Y> x, DetachedExpression<? extends Y> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.COALESCE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#coalesce(Expression, Object) 
   */
  public static <Y> DetachedExpression<Y> coalesce(DetachedExpression<? extends Y> x, Y y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.COALESCE, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#nullif(Expression, Expression) 
   */
  public static <Y> DetachedExpression<Y> nullif(DetachedExpression<Y> x, DetachedExpression<?> y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.NULL_IF, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#nullif(Expression, Object) 
   */
  public static <Y> DetachedExpression<Y> nullif(DetachedExpression<Y> x, Y y) {
    return new DetachedExpressionImpl<>(DetachedExpressionType.NULL_IF, List.of(x, y));
  }

  /**
   * @see CriteriaBuilder#coalesce() 
   */
  public static <T> DetachedCoalesce<T> coalesce() {
    return new DetachedCoalesceImpl<>();
  }

  /**
   * @see CriteriaBuilder#selectCase(Expression) 
   */
  public static <C, R> DetachedSimpleCase<C, R> selectCase(DetachedExpression<? extends C> expression) {
    return new DetachedSimpleCaseImpl<>(expression);
  }

  /**
   * @see CriteriaBuilder#selectCase() 
   */
  public static <R> DetachedCase<R> selectCase() {
    return new DetachedCaseImpl<>();
  }

  /**
   * @see CriteriaBuilder#function(String, Class, Expression[])
   */
  public static <T> DetachedExpression<T> function(String name, Class<T> type, DetachedExpression<?>... args) {
    List<Object> arguments = new ArrayList<>();
    arguments.add(name);
    arguments.add(type);
    arguments.addAll(Arrays.asList(args));
    return new DetachedExpressionImpl<>(DetachedExpressionType.FUNCTION, arguments);
  }
}
