package kirill.detachedjpacriteria.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPath;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedPathImpl;
import kirill.detachedjpacriteria.expression.impl.ExpressionConverterContext;
import kirill.detachedjpacriteria.expression.impl.PathContext;
import kirill.detachedjpacriteria.query.api.DetachedCommonCriteria;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaUpdate;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;

public class DetachedCriteriaUpdateImpl<T> extends AbstractDetachedCommonCriteria<CriteriaUpdate<T>, Query> implements DetachedCriteriaUpdate<T> {
  private final Class<T> entityClass;
  private final List<SetPair> setPairs = new ArrayList<>();

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedCriteriaUpdateImpl(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public DetachedCriteriaUpdate<T> where(DetachedExpression<Boolean> expression) {
    whereImpl(expression);
    return this;
  }

  @Override
  public DetachedCriteriaUpdate<T> where(DetachedPredicate... predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public DetachedCriteriaUpdate<T> where(Iterable<DetachedPredicate> predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public <X> DetachedCriteriaUpdate<T> parameter(String name, X value) {
    parameterImpl(name, value);
    return this;
  }

  @Override
  public <X> DetachedCriteriaUpdate<T> nullableParameter(String name, X value, Class<X> valueClass) {
    nullableParameterImpl(name, value, valueClass);
    return this;
  }

  @Override
  public CriteriaUpdate<T> createJpaCriteriaQuery(EntityManager entityManager) {
    return createJpaCriteriaQueryImpl(entityManager);
  }

  @Override
  public Query createJpaQuery(EntityManager entityManager) {
    return createJpaQueryImpl(entityManager);
  }

  @Override
  public DetachedCriteriaQuery<Long> toCountCriteriaQuery(DetachedExpression<?> countExpression) {
    DetachedCriteriaQuery<Long> selectCountQuery = DetachedCriteriaBuilder.selectCount(countExpression, entityClass);
    selectCountQuery.copyFromOtherCriteria(this, QueryCopyPart.COPY_WHERE, QueryCopyPart.COPY_PARAMS);
    return selectCountQuery;
  }

  @Override
  public DetachedCriteriaQuery<Long> toCountDistinctCriteriaQuery(DetachedExpression<?> countExpression) {
    DetachedCriteriaQuery<Long> selectCountQuery = DetachedCriteriaBuilder.selectCountDistinct(countExpression, entityClass);
    selectCountQuery.copyFromOtherCriteria(this, QueryCopyPart.COPY_WHERE, QueryCopyPart.COPY_PARAMS);
    return selectCountQuery;
  }

  @Override
  public List<CriteriaUpdate<T>> createJpaCriteriaBatchQueries(EntityManager entityManager, int batchSize) {
    return createJpaCriteriaBatchQueriesImpl(entityManager, batchSize);
  }

  @Override
  public List<Query> createJpaBatchQueries(EntityManager entityManager, int batchSize) {
    return createJpaBatchQueriesImpl(entityManager, batchSize);
  }

  @Override
  public void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts) {
    copyFromOtherCriteriaImpl(otherCriteria, copyParts);
  }

  @Override
  public <Y, X extends Y> DetachedCriteriaUpdate<T> set(DetachedPath<Y> attribute, X value) {
    setPairs.add(new SetPair(SetPairMode.PATH_AND_SIMPLE_VALUE, attribute, value));
    return this;
  }

  @Override
  public <Y> DetachedCriteriaUpdate<T> set(DetachedPath<Y> attribute, DetachedExpression<? extends Y> value) {
    setPairs.add(new SetPair(SetPairMode.PATH_AND_EXPRESSION_VALUE, attribute, value));
    return this;
  }

  @Override
  public DetachedCriteriaUpdate<T> set(String attributeName, Object value) {
    setPairs.add(new SetPair(SetPairMode.NAME_AND_SIMPLE_VALUE, attributeName, value));
    return this;
  }

  @Override
  CriteriaUpdate<T> createJpaCriteriaQuery(EntityManager entityManager, List<?> inValuesToReplace) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaUpdate<T> criteriaQuery = criteriaBuilder.createCriteriaUpdate(entityClass);
    Root<?> root = criteriaQuery.from(entityClass);

    ExpressionConverterContext context = new ExpressionConverterContext(
        new PathContext(root, Map.of(), Map.of()),
        null,
        parameters,
        inValuesToReplace,
        criteriaBuilder,
        entityManager
    );
    context.setCriteria(criteriaQuery);
    applySetPais(criteriaQuery, context);
    setWhere(criteriaQuery, context);

    return criteriaQuery;
  }

  private <Y, X extends Y> void applySetPais(CriteriaUpdate<T> criteriaQuery, ExpressionConverterContext context) {
    for (SetPair setPair : setPairs) {
      switch (setPair.mode) {
        case PATH_AND_SIMPLE_VALUE:
          //Норм что супрессим потому что проверки идут через сигнатуры вызовов.
          //noinspection unchecked
          criteriaQuery.set((Path<Y>) setPair.getAttributeAsPath().toJpaExpression(context), (X) setPair.value);
          break;
        case PATH_AND_EXPRESSION_VALUE:
          //Норм что супрессим потому что проверки идут через сигнатуры вызовов.
          //noinspection unchecked
          criteriaQuery.set(
              (Path<Y>) setPair.getAttributeAsPath().toJpaExpression(context),
              (Expression<? extends Y>) setPair.getValueAsExpression().toJpaExpression(context)
          );
          break;
        case NAME_AND_SIMPLE_VALUE:
          criteriaQuery.set((String) setPair.attribute, setPair.value);
          break;
        default:
          throw new IllegalStateException(String.format("Unexpected set pair mode %s", setPair.mode));
      }
    }
  }

  private void setWhere(CriteriaUpdate<T> criteriaQuery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = getSingleJpaExpression(whereExpressions, wherePredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaQuery.where(jpaExpression);
    } else {
      getSingleJpaPredicate(whereExpressions, wherePredicates, context).ifPresent(criteriaQuery::where);
    }
  }

  @Override
  Query createJpaQueryForJpaCriteriaQuery(CriteriaUpdate<T> criteriaQuery, EntityManager entityManager) {
    return entityManager.createQuery(criteriaQuery);
  }

  private static class SetPair {
    private final SetPairMode mode;
    private final Object attribute;
    private final Object value;

    SetPair(SetPairMode mode, Object attribute, Object value) {
      this.mode = mode;
      this.attribute = attribute;
      this.value = value;
    }

    DetachedPathImpl<?> getAttributeAsPath() {
      return (DetachedPathImpl<?>) attribute;
    }

    DetachedExpressionImpl<?> getValueAsExpression() {
      return (DetachedExpressionImpl<?>) value;
    }
  }

  private enum SetPairMode {
    PATH_AND_SIMPLE_VALUE,
    PATH_AND_EXPRESSION_VALUE,
    NAME_AND_SIMPLE_VALUE
  }
}
