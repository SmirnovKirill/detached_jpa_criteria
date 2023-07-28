package kirill.detachedjpacriteria.expression.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.persistence.criteria.Expression;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.expression.api.extra.DetachedIn;

public class DetachedExpressionImpl<T> implements DetachedExpression<T>, DetachedExpressionCommonImpl<T> {
  private final DetachedExpressionType type;
  /*
   * Аргументы, которые передаются сразу напрямую в метод для создания выражения через DetachedCriteriaBuilder. Есть еще дополнительные аргументы,
   * которые довводятся у самого объекта-выражения, пример это выражения DetachedIn, DetachedCase и т.д.
   */
  protected final List<?> directArguments;
  protected volatile String alias;
  protected volatile Class<T> asClass;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedExpressionImpl(DetachedExpressionType type, List<?> directArguments) {
    this.type = type;
    this.directArguments = directArguments;
  }

  protected DetachedExpressionImpl(DetachedExpressionType type, List<?> directArguments, String alias, Class<T> asClass) {
    this.type = type;
    this.directArguments = directArguments;
    this.alias = alias;
    this.asClass = asClass;
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
  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedExpressionImpl<>(this.type, directArguments, alias, type);
  }

  @Override
  public DetachedExpression<T> alias(String name) {
    alias = name;
    return this;
  }

  /**
   * Рекурсивная конвертация detached выражения и всех его подвыражений в выражения JPA Criteria.
   */
  @Override
  public Expression<T> toJpaExpression(ExpressionConverterContext context) {
    List<Expression<?>> directArgumentExpressions = new ArrayList<>();
    for (Object directArgument : directArguments) {
      if (directArgument instanceof DetachedExpression) {
        directArgumentExpressions.add(((DetachedExpressionCommonImpl<?>) directArgument).toJpaExpression(context));
      } else {
        directArgumentExpressions.add(context.getCriteriaBuilder().literal(directArgument));
      }
    }

    Expression<?> result = type.getConverter().convert(this, type, directArguments, directArgumentExpressions, context);
    if (asClass != null) {
      result = result.as(asClass);
    }

    if (alias != null) {
      result.alias(alias);
    }

    //noinspection unchecked
    return (Expression<T>) result;
  }

  public List<?> getDirectArguments() {
    return directArguments;
  }

  public List<?> getExtraArguments() {
    return List.of();
  }

  public DetachedExpressionType getType() {
    return type;
  }

  /**
   * Возвращает плоский список дерева состоящего из данного выражения и всех его подвыражений. Полезно если хочется найти какое-нибудь выражение или
   * сделать фильтрацию, а с древовидной структурой работать не так удобно.
   */
  @Override
  public List<DetachedExpressionCommonImpl<?>> getAllExpressionsDeep() {
    List<DetachedExpressionCommonImpl<?>> result = new ArrayList<>();

    result.add(this);

    List<Object> directAndExtraArguments = new ArrayList<>(getDirectArguments());
    directAndExtraArguments.addAll(getExtraArguments());
    for (Object argument : directAndExtraArguments) {
      if (argument instanceof DetachedExpression) {
        result.addAll(((DetachedExpressionCommonImpl<?>) argument).getAllExpressionsDeep());
      }
    }

    return result;
  }
}
