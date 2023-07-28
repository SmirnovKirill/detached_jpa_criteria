package kirill.detachedjpacriteria.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.impl.AttributePath;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionCommonImpl;
import kirill.detachedjpacriteria.expression.impl.ExpressionConverterContext;
import kirill.detachedjpacriteria.expression.impl.PathContext;
import kirill.detachedjpacriteria.query.api.DetachedCommonCriteria;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;
import kirill.detachedjpacriteria.util.Util;

public class DetachedCriteriaQueryImpl<T> extends AbstractDetachedCommonCriteria<CriteriaQuery<T>, TypedQuery<T>>
    implements DetachedCriteriaQuery<T>, CriteriaQueryWithDistinct, CriteriaQueryWithFetches, CriteriaQueryWithJoins, CriteriaQueryWithGrouping,
    CriteriaQueryWithOrder {
  private final SelectMode selectMode;
  private final List<DetachedExpression<?>> selectExpressions;
  private final Class<T> resultClass;
  private final Class<?> fromClass;
  private boolean distinct;
  private final List<Fetch> fetches = new ArrayList<>();
  private final List<Join> joins = new ArrayList<>();
  private final List<DetachedExpression<?>> groupByExpressions = new ArrayList<>();
  private final List<DetachedExpression<Boolean>> havingExpressions = new ArrayList<>();
  private final List<DetachedPredicate> havingPredicates = new ArrayList<>();
  private final List<Order> orders = new ArrayList<>();

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedCriteriaQueryImpl(
      SelectMode selectMode,
      List<DetachedExpression<?>> selectExpressions,
      Class<T> resultClass,
      Class<?> fromClass
  ) {
    this.selectMode = selectMode;
    this.selectExpressions = selectExpressions;
    this.resultClass = resultClass;
    this.fromClass = fromClass;
  }

  @Override
  public DetachedCriteriaQuery<T> where(DetachedExpression<Boolean> expression) {
    whereImpl(expression);
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> where(DetachedPredicate... predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> where(Iterable<DetachedPredicate> predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public <X> DetachedCriteriaQuery<T> parameter(String name, X value) {
    parameterImpl(name, value);
    return this;
  }

  @Override
  public <X> DetachedCriteriaQuery<T> nullableParameter(String name, X value, Class<X> valueClass) {
    nullableParameterImpl(name, value, valueClass);
    return this;
  }

  @Override
  public CriteriaQuery<T> createJpaCriteriaQuery(EntityManager entityManager) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(resultClass);
    Root<?> root = criteriaQuery.from(fromClass);

    Map<AttributePath, Path<?>> fetchPaths = new HashMap<>();
    Map<AttributePath, Path<?>> joinPaths = new HashMap<>();
    /* Важно чтобы фетчи и джоины шли до мест где могут быть выражения потому что выражения иначе могут не сработать (когда идем по коллекциям). */
    setFetch(root, fetchPaths);
    setJoin(root, joinPaths);

    ExpressionConverterContext context = new ExpressionConverterContext(
        new PathContext(root, fetchPaths, joinPaths),
        null,
        parameters,
        criteriaBuilder,
        entityManager
    );
    context.setCriteria(criteriaQuery);
    setSelect(criteriaQuery, context);
    criteriaQuery.distinct(distinct);

    addWhere(criteriaQuery, context);
    addGroupBy(criteriaQuery, context);
    addHaving(criteriaQuery, context);
    addOrderBy(criteriaQuery, context);

    return criteriaQuery;
  }

  private void setFetch(Root<?> root, Map<AttributePath, Path<?>> fetchPaths) {
    fetches.forEach(fetch -> fetchDeep(root, fetch, AttributePath.emptyPath(), fetchPaths));
  }

  private <X> void fetchDeep(FetchParent<X, X> parent, Fetch fetch, AttributePath parentAttributePath, Map<AttributePath, Path<?>> fetchPaths) {
    AttributePath childAttributePath = parentAttributePath.with(fetch.getAttributeName());

    FetchParent<X, X> child = parent.fetch(fetch.getAttributeName(), fetch.getJoinType());
    if (child instanceof Path) {
      fetchPaths.put(childAttributePath, (Path<?>) child);
    }

    fetch.getChildren().forEach(fetchChild -> fetchDeep(child, fetchChild, childAttributePath, fetchPaths));
  }

  private void setJoin(Root<?> root, Map<AttributePath, Path<?>> joinPaths) {
    joins.forEach(join -> joinDeep(root, join, AttributePath.emptyPath(), joinPaths));
  }

  private void setSelect(CriteriaQuery<T> criteriaQuery, ExpressionConverterContext context) {
    if (selectExpressions == null || selectExpressions.isEmpty()) {
      return;
    }

    List<Selection<?>> jpaSelectExpressions = selectExpressions.stream()
        .map(expression -> (DetachedExpressionCommonImpl<?>) expression)
        .map(expression -> expression.toJpaExpression(context))
        .collect(Collectors.toList());
    if (selectMode == SelectMode.SINGLE) {
      //Норм супрессить потому что тип контролируется при создании билдера.
      //noinspection unchecked
      criteriaQuery.select((Selection<? extends T>) jpaSelectExpressions.get(0));
    } else if (selectMode == SelectMode.MULTI) {
      criteriaQuery.multiselect(jpaSelectExpressions);
    } else {
      throw new IllegalStateException(String.format("Unexpected select mode %s, most likely a bug", selectMode));
    }
  }

  private void addWhere(CriteriaQuery<T> criteriaQuery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = getSingleJpaExpression(whereExpressions, wherePredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaQuery.where(jpaExpression);
    } else {
      getSingleJpaPredicate(whereExpressions, wherePredicates, context).ifPresent(criteriaQuery::where);
    }
  }

  private void addGroupBy(CriteriaQuery<T> criteriaQuery, ExpressionConverterContext context) {
    if (groupByExpressions.size() == 0) {
      return;
    }

    criteriaQuery.groupBy(
        groupByExpressions.stream()
            .map(expression -> (DetachedExpressionCommonImpl<?>) expression)
            .map(expression -> expression.toJpaExpression(context))
            .collect(Collectors.toList())
    );
  }

  private void addHaving(CriteriaQuery<T> criteriaQuery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = getSingleJpaExpression(havingExpressions, havingPredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaQuery.having(jpaExpression);
    } else {
      getSingleJpaPredicate(havingExpressions, havingPredicates, context).ifPresent(criteriaQuery::having);
    }
  }

  private void addOrderBy(CriteriaQuery<T> criteriaQuery, ExpressionConverterContext context) {
    List<javax.persistence.criteria.Order> orderList = new ArrayList<>();

    for (Order order : orders) {
      Expression<?> jpaExpression = ((DetachedExpressionCommonImpl<?>) order.getExpression()).toJpaExpression(context);
      switch (order.getOrderBy()) {
        case ASC -> orderList.add(context.getCriteriaBuilder().asc(jpaExpression));
        case DESC -> orderList.add(context.getCriteriaBuilder().desc(jpaExpression));
        default -> throw new IllegalStateException(String.format("Unexpected order %s, most likely a bug", order.getOrderBy()));
      }
    }

    criteriaQuery.orderBy(orderList);
  }

  @Override
  public TypedQuery<T> createJpaQuery(EntityManager entityManager) {
    return createJpaQueryImpl(entityManager);
  }

  @Override
  public DetachedCriteriaQuery<Long> toCountCriteriaQuery(DetachedExpression<?> countExpression) {
    return toCountCriteriaQueryImpl(false, countExpression);
  }

  @Override
  public DetachedCriteriaQuery<Long> toCountDistinctCriteriaQuery(DetachedExpression<?> countExpression) {
    return toCountCriteriaQueryImpl(true, countExpression);
  }

  private DetachedCriteriaQuery<Long> toCountCriteriaQueryImpl(boolean distinct, DetachedExpression<?> countExpression) {
    if (!joins.isEmpty() && !fetches.isEmpty()) {
      /*
       * Надо объединить два дерева, но на практике это вряд ли кому-то нужно так что пока можно не делать. Оставить как есть нельзя потому что не
       * будет селектиться то что фетчим.
       */
      throw new UnsupportedOperationException("Count when there are joins and fetches is not yet implemented, please contact library developers");
    }

    if (!havingExpressions.isEmpty() || !havingPredicates.isEmpty()|| !groupByExpressions.isEmpty()) {
      throw new IllegalStateException("Can't count when grouping is involved");
    }

    DetachedCriteriaQuery<Long> selectCountQuery = distinct
        ? DetachedCriteriaBuilder.selectCountDistinct(countExpression, fromClass)
        : DetachedCriteriaBuilder.selectCount(countExpression, fromClass);

    List<Join> joins = !this.joins.isEmpty() ? this.joins : fetches.stream().map(Fetch::toJoinDeep).collect(Collectors.toList());
    selectCountQuery.join(joins);
    selectCountQuery.copyFromOtherCriteria(this, QueryCopyPart.COPY_WHERE, QueryCopyPart.COPY_PARAMS);

    return selectCountQuery;
  }

  @Override
  public void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts) {
    copyFromOtherCriteriaImpl(otherCriteria, copyParts);

    Set<QueryCopyPart> copyPartSet = Arrays.stream(copyParts).collect(Collectors.toSet());

    if (copyPartSet.contains(QueryCopyPart.COPY_DISTINCT) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithDistinct castedCriteria) {
        this.distinct = castedCriteria.isDistinct();
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_FETCH) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithFetches castedCriteria) {
        this.fetches.clear();
        this.fetches.addAll(castedCriteria.getFetches());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_JOIN) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithJoins castedCriteria) {
        this.joins.clear();
        this.joins.addAll(castedCriteria.getJoins());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_GROUP_BY) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithGrouping castedCriteria) {
        this.groupByExpressions.clear();
        this.groupByExpressions.addAll(castedCriteria.getGroupByExpressions());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_HAVING) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithGrouping castedCriteria) {
        this.havingExpressions.clear();
        this.havingExpressions.addAll(castedCriteria.getHavingExpressions());
        this.havingPredicates.clear();
        this.havingPredicates.addAll(castedCriteria.getHavingPredicates());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_ORDER) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithOrder castedCriteria) {
        this.orders.clear();
        this.orders.addAll(castedCriteria.getOrders());
      }
    }
  }

  @Override
  public DetachedCriteriaQuery<T> groupBy(DetachedExpression<?>... groupByExpressions) {
    return groupBy(List.of(groupByExpressions));
  }

  @Override
  public DetachedCriteriaQuery<T> groupBy(List<DetachedExpression<?>> groupByExpressions) {
    this.groupByExpressions.addAll(groupByExpressions);
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> having(DetachedExpression<Boolean> expression) {
    havingExpressions.add(expression);
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> having(DetachedPredicate... predicates) {
    return having(Arrays.asList(predicates));
  }

  @Override
  public DetachedCriteriaQuery<T> having(Iterable<DetachedPredicate> predicates) {
    wherePredicates.addAll(Util.toList(predicates));
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> orderByAsc(DetachedExpression<?>... expressions) {
    return orderByAsc(Arrays.asList(expressions));
  }

  @Override
  public DetachedCriteriaQuery<T> orderByAsc(List<DetachedExpression<?>> expressions) {
    return orderBy(OrderBy.ASC, expressions);
  }

  @Override
  public DetachedCriteriaQuery<T> orderByDesc(DetachedExpression<?>... expressions) {
    return orderByDesc(Arrays.asList(expressions));
  }

  @Override
  public DetachedCriteriaQuery<T> orderByDesc(List<DetachedExpression<?>> expressions) {
    return orderBy(OrderBy.DESC, expressions);
  }

  @Override
  public DetachedCriteriaQuery<T> orderBy(OrderBy orderBy, DetachedExpression<?>... expressions) {
    return orderBy(orderBy, Arrays.asList(expressions));
  }

  @Override
  public DetachedCriteriaQuery<T> orderBy(OrderBy orderBy, List<DetachedExpression<?>> expressions) {
    this.orders.addAll(expressions.stream().map(expression -> new Order(orderBy, expression)).toList());
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> distinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> innerFetch(String... attributeNames) {
    return fetch(Arrays.stream(attributeNames).map(Fetch::innerFetch).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaQuery<T> leftFetch(String... attributeNames) {
    return fetch(Arrays.stream(attributeNames).map(Fetch::leftFetch).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaQuery<T> fetch(Fetch... fetches) {
    return fetch(Arrays.asList(fetches));
  }

  @Override
  public DetachedCriteriaQuery<T> fetch(Iterable<Fetch> fetches) {
    this.fetches.addAll(Util.toList(fetches));
    return this;
  }

  @Override
  public DetachedCriteriaQuery<T> innerJoin(String... attributeNames) {
    return join(Arrays.stream(attributeNames).map(Join::innerJoin).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaQuery<T> leftJoin(String... attributeNames) {
    return join(Arrays.stream(attributeNames).map(Join::leftJoin).collect(Collectors.toList()));
  }

  @Override
  public DetachedCriteriaQuery<T> join(Join... joins) {
    return join(Arrays.asList(joins));
  }

  @Override
  public DetachedCriteriaQuery<T> join(Iterable<Join> joins) {
    this.joins.addAll(Util.toList(joins));
    return this;
  }

  @Override
  public TypedQuery<T> createJpaQueryForJpaCriteriaQuery(CriteriaQuery<T> criteriaQuery, EntityManager entityManager) {
    return entityManager.createQuery(criteriaQuery);
  }

  @Override
  public boolean isDistinct() {
    return distinct;
  }

  @Override
  public List<Fetch> getFetches() {
    return fetches;
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
  public List<Order> getOrders() {
    return orders;
  }

  public static class FirstStepQueryBuilderSingleWithType<T> implements DetachedCriteriaQuery.FirstStepQueryBuilderSingleWithType<T> {
    private final DetachedExpression<T> selectExpression;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public FirstStepQueryBuilderSingleWithType(DetachedExpression<T> selectExpression) {
      this.selectExpression = selectExpression;
    }

    @Override
    public DetachedCriteriaQuery.SecondStepQueryBuilder<T> into(Class<T> resultClass) {
      return new SecondStepQueryBuilder<>(SelectMode.SINGLE, List.of(selectExpression), resultClass);
    }
  }

  public static class FirstStepQueryBuilderSingleWithoutType implements DetachedCriteriaQuery.FirstStepQueryBuilderSingleWithoutType {
    private final DetachedExpression<?> selectExpression;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public FirstStepQueryBuilderSingleWithoutType(DetachedExpression<?> selectExpression) {
      this.selectExpression = selectExpression;
    }

    @Override
    public <T> DetachedCriteriaQuery.SecondStepQueryBuilder<T> into(Class<T> resultClass) {
      return new SecondStepQueryBuilder<>(SelectMode.SINGLE, List.of(selectExpression), resultClass);
    }
  }

  public static class FirstStepQueryBuilderMulti implements DetachedCriteriaQuery.FirstStepQueryBuilderMulti {
    private final List<DetachedExpression<?>> selectExpressions;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public FirstStepQueryBuilderMulti(List<DetachedExpression<?>> selectExpressions) {
      this.selectExpressions = selectExpressions;
    }

    @Override
    public <T> DetachedCriteriaQuery.SecondStepQueryBuilder<T> into(Class<T> resultClass) {
      return new SecondStepQueryBuilder<>(SelectMode.MULTI, selectExpressions, resultClass);
    }

    @Override
    public DetachedCriteriaQuery.SecondStepQueryBuilder<Tuple> intoTuple() {
      return new SecondStepQueryBuilder<>(SelectMode.MULTI, selectExpressions, Tuple.class);
    }
  }

  public static class SecondStepQueryBuilder<T> implements DetachedCriteriaQuery.SecondStepQueryBuilder<T> {
    private final SelectMode selectMode;
    private final List<DetachedExpression<?>> selectExpressions;
    private final Class<T> resultClass;

    /**
     * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
     */
    public SecondStepQueryBuilder(SelectMode selectMode, List<DetachedExpression<?>> selectExpressions, Class<T> resultClass) {
      this.selectMode = selectMode;
      this.selectExpressions = selectExpressions;
      this.resultClass = resultClass;
    }

    @Override
    public DetachedCriteriaQuery<T> from(Class<?> fromClass) {
      return new DetachedCriteriaQueryImpl<>(selectMode, selectExpressions, resultClass, fromClass);
    }
  }

  public enum SelectMode {
    SINGLE,
    MULTI
  }

  static class Order{
    private final OrderBy orderBy;
    private final DetachedExpression<?> expression;

    Order(OrderBy orderBy, DetachedExpression<?> expression) {
      this.orderBy = orderBy;
      this.expression = expression;
    }

    OrderBy getOrderBy() {
      return orderBy;
    }


    DetachedExpression<?> getExpression() {
      return expression;
    }
  }
}
