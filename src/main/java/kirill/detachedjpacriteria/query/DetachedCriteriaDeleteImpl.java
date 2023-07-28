package kirill.detachedjpacriteria.query;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.impl.ExpressionConverterContext;
import kirill.detachedjpacriteria.expression.impl.PathContext;
import kirill.detachedjpacriteria.query.api.DetachedCommonCriteria;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaDelete;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;

public class DetachedCriteriaDeleteImpl<T> extends AbstractDetachedCommonCriteria<CriteriaDelete<T>, Query> implements DetachedCriteriaDelete<T> {
  private final Class<T> entityClass;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedCriteriaDeleteImpl(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public DetachedCriteriaDelete<T> where(DetachedExpression<Boolean> expression) {
    whereImpl(expression);
    return this;
  }

  @Override
  public DetachedCriteriaDelete<T> where(DetachedPredicate... predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public DetachedCriteriaDelete<T> where(Iterable<DetachedPredicate> predicates) {
    whereImpl(predicates);
    return this;
  }

  @Override
  public <X> DetachedCriteriaDelete<T> parameter(String name, X value) {
    parameterImpl(name, value);
    return this;
  }

  @Override
  public <X> DetachedCriteriaDelete<T> nullableParameter(String name, X value, Class<X> valueClass) {
    nullableParameterImpl(name, value, valueClass);
    return this;
  }

  @Override
  public CriteriaDelete<T> createJpaCriteriaQuery(EntityManager entityManager) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaDelete<T> criteriaQuery = criteriaBuilder.createCriteriaDelete(entityClass);
    Root<?> root = criteriaQuery.from(entityClass);

    ExpressionConverterContext context = new ExpressionConverterContext(
        new PathContext(root, Map.of(), Map.of()),
        null,
        parameters,
        criteriaBuilder,
        entityManager
    );
    context.setCriteria(criteriaQuery);
    setWhere(criteriaQuery, context);

    return criteriaQuery;
  }

  private void setWhere(CriteriaDelete<T> criteriaQuery, ExpressionConverterContext context) {
    Expression<Boolean> jpaExpression = getSingleJpaExpression(whereExpressions, wherePredicates, context).orElse(null);
    if (jpaExpression != null) {
      criteriaQuery.where(jpaExpression);
    } else {
      getSingleJpaPredicate(whereExpressions, wherePredicates, context).ifPresent(criteriaQuery::where);
    }
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
  public void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts) {
    copyFromOtherCriteriaImpl(otherCriteria, copyParts);
  }

  @Override
  Query createJpaQueryForJpaCriteriaQuery(CriteriaDelete<T> criteriaQuery, EntityManager entityManager) {
    return entityManager.createQuery(criteriaQuery);
  }
}
