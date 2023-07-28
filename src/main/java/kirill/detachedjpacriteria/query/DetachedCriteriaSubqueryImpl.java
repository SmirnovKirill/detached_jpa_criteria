package kirill.detachedjpacriteria.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.api.extra.DetachedIn;
import kirill.detachedjpacriteria.expression.impl.AttributePath;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionCommonImpl;
import kirill.detachedjpacriteria.expression.impl.ExpressionConverterContext;
import kirill.detachedjpacriteria.expression.impl.PathContext;
import kirill.detachedjpacriteria.query.api.DetachedCommonCriteria;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaSubquery;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;
import kirill.detachedjpacriteria.util.Util;

public class DetachedCriteriaSubqueryImpl<T> implements DetachedCriteriaSubquery<T>, DetachedExpressionCommonImpl<T>, CriteriaQueryWithDistinct,
    CriteriaQueryWithJoins, CriteriaQueryWithGrouping, CriteriaQueryWithWhere {
  private final DetachedExpression<?> selectExpression;
  private final Class<T> resultClass;
  private final Class<?> fromClass;
  private boolean distinct;
  private final List<Join> joins;
  final List<DetachedExpression<Boolean>> whereExpressions;
  final List<DetachedPredicate> wherePredicates;
  private final List<DetachedExpression<?>> groupByExpressions;
  private final List<DetachedExpression<Boolean>> havingExpressions;
  private final List<DetachedPredicate> havingPredicates;
  private volatile String alias;
  private volatile Class<T> asClass;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedCriteriaSubqueryImpl(DetachedExpression<?> selectExpression, Class<T> resultClass, Class<?> fromClass) {
    this.selectExpression = selectExpression;
    this.resultClass = resultClass;
    this.fromClass = fromClass;
    this.joins = new ArrayList<>();
    this.whereExpressions = new ArrayList<>();
    this.wherePredicates = new ArrayList<>();
    this.groupByExpressions = new ArrayList<>();
    this.havingExpressions = new ArrayList<>();
    this.havingPredicates = new ArrayList<>();
  }

  private DetachedCriteriaSubqueryImpl(
      DetachedExpression<T> selectExpression,
      Class<T> resultClass,
      Class<?> fromClass,
      boolean distinct,
      List<Join> joins,
      List<DetachedExpression<Boolean>> whereExpressions,
      List<DetachedPredicate> wherePredicates,
      List<DetachedExpression<?>> groupByExpressions,
      List<DetachedExpression<Boolean>> havingExpressions,
      List<DetachedPredicate> havingPredicates,
      String alias,
      Class<T> asClass
  ) {
    this.selectExpression = selectExpression;
    this.resultClass = resultClass;
    this.fromClass = fromClass;
    this.distinct = distinct;
    this.joins = joins;
    this.whereExpressions = whereExpressions;
    this.wherePredicates = wherePredicates;
    this.groupByExpressions = groupByExpressions;
    this.havingExpressions = havingExpressions;
    this.havingPredicates = havingPredicates;
    this.alias = alias;
    this.asClass = asClass;
  }

  @Override
  public DetachedCriteriaSubqueryImpl<T> where(DetachedExpression<Boolean> expression) {
    whereExpressions.add(expression);
    return this;
  }

  @Override
  public DetachedCriteriaSubqueryImpl<T> where(DetachedPredicate... predicates) {
    return where(Arrays.asList(predicates));
  }

  @Override
  public DetachedCriteriaSubqueryImpl<T> where(Iterable<DetachedPredicate> predicates) {
    this.wherePredicates.addAll(Util.toList(predicates));
    return this;
  }

  @Override
  public DetachedCriteriaSubquery<T> groupBy(DetachedExpression<?>... groupByExpressions) {
    return groupBy(List.of(groupByExpressions));
  }

  @Override
  public DetachedCriteriaSubquery<T> groupBy(List<DetachedExpression<?>> groupByExpressions) {
    this.groupByExpressions.addAll(groupByExpressions);
    return this;
  }

  @Override
  public DetachedCriteriaSubquery<T> having(DetachedExpression<Boolean> expression) {
    havingExpressions.add(expression);
    return this;
  }

  @Override
  public DetachedCriteriaSubquery<T> having(DetachedPredicate... predicates) {
    return having(Arrays.asList(predicates));
  }

  @Override
  public DetachedCriteriaSubquery<T> having(Iterable<DetachedPredicate> predicates) {
    wherePredicates.addAll(Util.toList(predicates));
    return this;
  }

  @Override
  public DetachedCriteriaSubquery<T> distinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  @Override
  public DetachedCriteriaSubquery<T> innerJoin(String... attributeNames) {
    return join(Arrays.stream(attributeNames).map(Join::innerJoin).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaSubquery<T> leftJoin(String... attributeNames) {
    return join(Arrays.stream(attributeNames).map(Join::leftJoin).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaSubquery<T> join(Join... joins) {
    return join(Arrays.asList(joins));
  }

  @Override
  public DetachedCriteriaSubquery<T> join(Iterable<Join> joins) {
    this.joins.addAll(Util.toList(joins));
    return this;
  }

  @Override
  public boolean isDistinct() {
    return distinct;
  }

  @Override
  public List<DetachedExpression<?>> getGroupByExpressions() {
    return groupByExpressions;
  }

  @Override
  public List<DetachedExpression<Boolean>> getHavingExpressions() {
    return havingExpressions;
  }

  @Override
  public List<DetachedPredicate> getHavingPredicates() {
    return havingPredicates;
  }

  @Override
  public List<Join> getJoins() {
    return joins;
  }

  @Override
  public List<DetachedExpression<Boolean>> getWhereExpressions() {
    return whereExpressions;
  }

  @Override
  public List<DetachedPredicate> getWherePredicates() {
    return wherePredicates;
  }

  @Override
  public void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts) {
    Set<QueryCopyPart> copyPartSet = Arrays.stream(copyParts).collect(Collectors.toSet());

    if (copyPartSet.contains(QueryCopyPart.COPY_WHERE) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithWhere) {
        CriteriaQueryWithWhere castedCriteria = (CriteriaQueryWithWhere) otherCriteria;
        this.whereExpressions.clear();
        this.whereExpressions.addAll(castedCriteria.getWhereExpressions());
        this.wherePredicates.clear();
        this.wherePredicates.addAll(castedCriteria.getWherePredicates());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_DISTINCT) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithDistinct) {
        CriteriaQueryWithDistinct castedCriteria = (CriteriaQueryWithDistinct) otherCriteria;
        this.distinct = castedCriteria.isDistinct();
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_JOIN) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithJoins) {
        CriteriaQueryWithJoins castedCriteria = (CriteriaQueryWithJoins) otherCriteria;
        this.joins.clear();
        this.joins.addAll(castedCriteria.getJoins());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_GROUP_BY) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithGrouping) {
        CriteriaQueryWithGrouping castedCriteria = (CriteriaQueryWithGrouping) otherCriteria;
        this.groupByExpressions.clear();
        this.groupByExpressions.addAll(castedCriteria.getGroupByExpressions());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_HAVING) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithGrouping) {
        CriteriaQueryWithGrouping castedCriteria = (CriteriaQueryWithGrouping) otherCriteria;
        this.havingExpressions.clear();
        this.havingExpressions.addAll(castedCriteria.getHavingExpressions());
        this.havingPredicates.clear();
        this.havingPredicates.addAll(castedCriteria.getHavingPredicates());
      }
    }
  }

  @Override
  public DetachedPredicate isNull() {
    return DetachedCriteriaBuilder.isNull(this);
  }

  @Override
  public DetachedPredicate isNotNull() {
    return DetachedCriteriaBuilder.isNotNull(this);
  }

  @Override
  public DetachedPredicate in(Object... values) {
    DetachedIn<Object> detachedIn = DetachedCriteriaBuilder.in(this);

    Arrays.stream(values).forEach(detachedIn::value);

    return detachedIn;
  }

  @Override
  public DetachedPredicate in(DetachedExpression<?>... values) {
    DetachedIn<Object> detachedIn = DetachedCriteriaBuilder.in(this);

    Arrays.stream(values).forEach(detachedIn::value);

    return detachedIn;
  }

  @Override
  public DetachedPredicate in(Collection<?> values) {
    DetachedIn<Object> detachedIn = DetachedCriteriaBuilder.in(this);

    detachedIn.value(values);

    return detachedIn;
  }

  @Override
  public DetachedPredicate in(DetachedExpression<Collection<?>> values) {
    DetachedIn<Object> detachedIn = DetachedCriteriaBuilder.in(this);

    detachedIn.value(values);

    return detachedIn;
  }

  @Override
  public DetachedCriteriaSubqueryImpl<T> alias(String name) {
    alias = name;
    return this;
  }

  @SuppressWarnings("unchecked") //Норм супрессить потому что в целом данный каст небезопасен.
  @Override
  public <X> DetachedCriteriaSubqueryImpl<X> as(Class<X> type) {
    return new DetachedCriteriaSubqueryImpl<>(
        (DetachedExpression<X>) selectExpression,
        (Class<X>) resultClass,
        fromClass,
        distinct,
        joins,
        whereExpressions,
        wherePredicates,
        groupByExpressions,
        havingExpressions,
        havingPredicates,
        alias,
        type
    );
  }

  @Override
  public Expression<T> toJpaExpression(ExpressionConverterContext parentQueryContext) {
    CriteriaBuilder criteriaBuilder = parentQueryContext.getCriteriaBuilder();

    Subquery<T> criteriaSubquery = parentQueryContext.getCriteria().subquery(resultClass);
    Root<?> root = criteriaSubquery.from(fromClass);

    Map<AttributePath, Path<?>> joinPaths = new HashMap<>();
    /* Важно чтобы джоины шли до мест где могут быть выражения потому что выражения иначе могут не сработать (когда идем по коллекциям). */
    joins.forEach(join -> AbstractDetachedCommonCriteria.joinDeep(root, join, AttributePath.emptyPath(), joinPaths));

    ExpressionConverterContext subqueryContext = new ExpressionConverterContext(
        new PathContext(root, Map.of(), joinPaths),
        parentQueryContext.getPathContext(),
        parentQueryContext.getParameters(),
        parentQueryContext.getInValuesToReplace(),
        criteriaBuilder,
        parentQueryContext.getEntityManager()
    );
    subqueryContext.setCriteria(criteriaSubquery);

    setSelect(criteriaSubquery, subqueryContext);
    criteriaSubquery.distinct(distinct);

    addWhere(criteriaSubquery, subqueryContext);
    addGroupBy(criteriaSubquery, subqueryContext);
    addHaving(criteriaSubquery, subqueryContext);

    Expression<T> queryAsExpression = criteriaSubquery;
    if (asClass != null) {
      queryAsExpression = criteriaSubquery.as(asClass);
    }

    if (alias != null) {
      queryAsExpression.alias(alias);
    }

    return queryAsExpression;
  }

  private void setSelect(Subquery<T> criteriaSubquery, ExpressionConverterContext context) {
    if (selectExpression == null) {
      return;
    }

    //Норм супрессить потому что тип контролируется при создании билдера.
    //noinspection unchecked
    criteriaSubquery.select(((DetachedExpressionCommonImpl<T>) selectExpression).toJpaExpression(context));
  }

  private void addWhere(Subquery<T> criteriaSubquery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = AbstractDetachedCommonCriteria.getSingleJpaExpression(whereExpressions, wherePredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaSubquery.where(jpaExpression);
    } else {
      AbstractDetachedCommonCriteria.getSingleJpaPredicate(whereExpressions, wherePredicates, context).ifPresent(criteriaSubquery::where);
    }
  }

  private void addGroupBy(Subquery<T> criteriaSubquery, ExpressionConverterContext context) {
    if (groupByExpressions.size() == 0) {
      return;
    }

    criteriaSubquery.groupBy(
        groupByExpressions.stream()
            .map(expression -> (DetachedExpressionCommonImpl<?>) expression)
            .map(expression -> expression.toJpaExpression(context))
            .collect(Collectors.toList())
    );
  }

  private void addHaving(Subquery<T> criteriaSubquery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = AbstractDetachedCommonCriteria.getSingleJpaExpression(havingExpressions, havingPredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaSubquery.having(jpaExpression);
    } else {
      AbstractDetachedCommonCriteria.getSingleJpaPredicate(havingExpressions, havingPredicates, context).ifPresent(criteriaSubquery::having);
    }
  }

  @Override
  public List<DetachedExpressionCommonImpl<?>> getAllExpressionsDeep() {
    return null;
  }

  public static class FirstStepQueryBuilderWithType<T> implements DetachedCriteriaSubquery.FirstStepQueryBuilderWithType<T> {
    private final DetachedExpression<T> selectExpression;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public FirstStepQueryBuilderWithType(DetachedExpression<T> selectExpression) {
      this.selectExpression = selectExpression;
    }

    @Override
    public DetachedCriteriaSubquery.SecondStepQueryBuilder<T> into(Class<T> resultClass) {
      return new SecondStepQueryBuilder<>(selectExpression, resultClass);
    }
  }

  public static class FirstStepQueryBuilderWithoutType implements DetachedCriteriaSubquery.FirstStepQueryBuilderWithoutType {
    private final DetachedExpression<?> selectExpression;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public FirstStepQueryBuilderWithoutType(DetachedExpression<?> selectExpression) {
      this.selectExpression = selectExpression;
    }

    @Override
    public <T> DetachedCriteriaSubquery.SecondStepQueryBuilder<T> into(Class<T> resultClass) {
      return new SecondStepQueryBuilder<>(selectExpression, resultClass);
    }
  }

  public static class SecondStepQueryBuilder<T> implements DetachedCriteriaSubquery.SecondStepQueryBuilder<T> {
    private final DetachedExpression<?> selectExpression;
    private final Class<T> resultClass;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public SecondStepQueryBuilder(DetachedExpression<?> selectExpression, Class<T> resultClass) {
      this.selectExpression = selectExpression;
      this.resultClass = resultClass;
    }

    @Override
    public DetachedCriteriaSubquery<T> from(Class<?> fromClass) {
      return new DetachedCriteriaSubqueryImpl<>(selectExpression, resultClass, fromClass);
    }
  }
}
