package kirill.detachedjpacriteria.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.impl.AttributePath;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionCommonImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedPredicateImpl;
import kirill.detachedjpacriteria.expression.impl.ExpressionConverterContext;
import kirill.detachedjpacriteria.query.api.DetachedCommonCriteria;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;
import kirill.detachedjpacriteria.util.Parameter;
import static kirill.detachedjpacriteria.util.Util.toList;

abstract class AbstractDetachedCommonCriteria<C extends CommonAbstractCriteria, Q extends Query>
    implements CriteriaQueryWithWhere, CriteriaQueryWithParameters {
  final List<DetachedExpression<Boolean>> whereExpressions = new ArrayList<>();
  final List<DetachedPredicate> wherePredicates = new ArrayList<>();
  final Map<String, Parameter<?>> parameters = new HashMap<>();

  void whereImpl(DetachedExpression<Boolean> expression) {
    whereExpressions.add(expression);
  }

  void whereImpl(DetachedPredicate... predicates) {
    whereImpl(Arrays.asList(predicates));
  }

  void whereImpl(Iterable<DetachedPredicate> predicates) {
    this.wherePredicates.addAll(toList(predicates));
  }

  <X> void parameterImpl(String name, X value) {
    if (value == null) {
      throw new IllegalStateException(
          "Parameter value has to be non null in order to extract value class. If values can be null then please use the nullableParameter() method."
      );
    }

    //noinspection unchecked
    parameters.put(name, new Parameter<>(name, (Class<X>) value.getClass(), value));
  }

  <X> void nullableParameterImpl(String name, X value, Class<X> valueClass) {
    parameters.put(name, new Parameter<>(name, valueClass, value));
  }

  Q createJpaQueryImpl(EntityManager entityManager) {
    C jpaCriteriaQuery = createJpaCriteriaQuery(entityManager);

    Q jpaQuery = createJpaQueryForJpaCriteriaQuery(jpaCriteriaQuery, entityManager);
    setParameters(jpaQuery);

    return jpaQuery;
  }

  void copyFromOtherCriteriaImpl(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts) {
    Set<QueryCopyPart> copyPartSet = Arrays.stream(copyParts).collect(Collectors.toSet());

    if (copyPartSet.contains(QueryCopyPart.COPY_WHERE) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithWhere castedCriteria) {

        this.whereExpressions.clear();
        this.whereExpressions.addAll(castedCriteria.getWhereExpressions());
        this.wherePredicates.clear();
        this.wherePredicates.addAll(castedCriteria.getWherePredicates());
      }
    }
    if (copyPartSet.contains(QueryCopyPart.COPY_PARAMS) || copyPartSet.contains(QueryCopyPart.COPY_ALL_FIELDS)) {
      if (otherCriteria instanceof CriteriaQueryWithParameters castedCriteria) {

        this.parameters.putAll(castedCriteria.getParameters());
      }
    }
  }

  //todo обязательно тест когда несколько expressions

  /**
   * Возвращает Expression<Boolean> если это было единственное выражение среди всех условий. Чтобы потом использовать вызовы JPA, где требуется ровно
   * один Expression<Boolean>.
   */
  static Optional<Expression<Boolean>> getSingleJpaExpression(
      List<DetachedExpression<Boolean>> expressions,
      List<DetachedPredicate> predicates,
      ExpressionConverterContext context
  ) {
    if (expressions.size() == 1 && predicates.isEmpty()) {
      //Ок супрессить, точно знаем что типы совпадут.
      //noinspection unchecked
      return Optional.of(((DetachedExpressionCommonImpl<Boolean>) expressions.get(0)).toJpaExpression(context));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Возвращает массив Predicate на основе переданных выражений и предикатов, если такой предикат можно построить. Это нужно, чтобы использовать потом
   * вызовы JPA, где требуется массив Predicate и не получилось использовать единственное Expression<Boolean>.
   */
  static Optional<Predicate[]> getSingleJpaPredicate(
      List<DetachedExpression<Boolean>> expressions,
      List<DetachedPredicate> predicates,
      ExpressionConverterContext context
  ) {
    if (expressions.isEmpty()) {
      if (predicates.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(
          predicates.stream().map(predicate -> (Predicate) ((DetachedPredicateImpl) predicate).toJpaExpression(context)).toArray(Predicate[]::new)
      );
    }

    if (expressions.size() + predicates.size() < 2) {
      return Optional.empty();
    }

    DetachedPredicate resultPredicate;
    if (predicates.isEmpty()) {
      resultPredicate = DetachedCriteriaBuilder.and(expressions.get(0), expressions.get(1));
      for (int i = 2; i < expressions.size(); i++) {
        resultPredicate = DetachedCriteriaBuilder.and(resultPredicate, expressions.get(i));
      }
    } else {
      resultPredicate = DetachedCriteriaBuilder.and(predicates.toArray(new DetachedPredicate[0]));
      for (DetachedExpression<Boolean> expression : expressions) {
        resultPredicate = DetachedCriteriaBuilder.and(resultPredicate, expression);
      }
    }

    return Optional.of(new Predicate[]{(Predicate) ((DetachedPredicateImpl) resultPredicate).toJpaExpression(context)});
  }

  void setParameters(Query query) {
    for (Parameter<?> parameter : parameters.values()) {
      query.setParameter(parameter.getName(), parameter.getValue());
    }
  }

  static <X> void joinDeep(From<X, X> parent, Join join, AttributePath parentAttributePath, Map<AttributePath, Path<?>> joinPaths) {
    AttributePath childAttributePath = parentAttributePath.with(join.getAttributeName());

    javax.persistence.criteria.Join<X, X> child = parent.join(join.getAttributeName(), join.getJoinType());
    joinPaths.put(childAttributePath, child);

    join.getChildren().forEach(joinChild -> joinDeep(child, joinChild, childAttributePath, joinPaths));
  }

  @Override
  public Map<String, Parameter<?>> getParameters() {
    return parameters;
  }

  @Override
  public List<DetachedExpression<Boolean>> getWhereExpressions() {
    return whereExpressions;
  }

  @Override
  public List<DetachedPredicate> getWherePredicates() {
    return wherePredicates;
  }

  abstract C createJpaCriteriaQuery(EntityManager entityManager);

  abstract Q createJpaQueryForJpaCriteriaQuery(C jpaCriteriaQuery, EntityManager entityManager);
}
