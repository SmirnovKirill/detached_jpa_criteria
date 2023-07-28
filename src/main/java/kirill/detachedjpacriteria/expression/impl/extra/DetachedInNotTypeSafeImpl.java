package kirill.detachedjpacriteria.expression.impl.extra;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionType;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedParameterExpression;
import kirill.detachedjpacriteria.expression.impl.DetachedParameterExpressionImpl;

/*
 * Два класса (а не только DetachedInImpl) было создано только ради того, чтобы поддержать метод .as() у in выражения. Иначе это невозможно т.к. в in
 * заложено, что он имплементирует expression с типом Boolean поэтому каст делать там нельзя. Этот же класс убирает завязку на Boolean за счет ввода
 * параметра S.
 */
public class DetachedInNotTypeSafeImpl<T, S> extends DetachedExpressionImpl<S> implements DetachedExpression<S> {
  protected final List<DetachedExpression<? extends T>> values;

  protected DetachedInNotTypeSafeImpl(
      List<?> directArguments,
      String alias,
      Class<S> asClass,
      List<DetachedExpression<? extends T>> values
  ) {
    super(DetachedExpressionType.IN, directArguments, alias, asClass);
    this.values = values;
  }

  @Override
  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedInNotTypeSafeImpl<>(directArguments, alias, type, values);
  }

  public List<DetachedExpression<? extends T>> getValues() {
    return values;
  }

  @Override
  public List<?> getExtraArguments() {
    return new ArrayList<>(values);
  }

  /**
   * Возвращает true если значения для IN должны браться из параметра.
   */
  public boolean isValueParameter() {
    return values.size() == 1 && values.get(0) instanceof DetachedParameterExpression;
  }

  public String getValueParameterName() {
    if (!isValueParameter()) {
      throw new IllegalStateException("IN expression doesn't contain parameters");
    }

    return ((DetachedParameterExpressionImpl<?>) values.get(0)).getName();
  }

  /**
   * Пробует вернуть список уникальных значений в списке. Это можно сделать если значение берется из параметра или если в качестве значений передавали
   * коллекции или же простые классы (даты, строки, примитивы).
   * @throws IllegalStateException если построить список простых значений невозможно
   */
  public List<T> getUniqueValuesAsList(Map<String, Object> parameters) {
    Set<T> values = new LinkedHashSet<>();

    if (isValueParameter()) {
      values.addAll(getValuesFromParameterExpression((DetachedParameterExpressionImpl<?>) this.values.get(0), parameters));
    } else {
      for (DetachedExpression<? extends T> valueExpression : this.values) {
        if (!(valueExpression instanceof DetachedExpressionImpl)) {
          throw new IllegalStateException(String.format("Unsupported value expression class %s", valueExpression.getClass().getName()));
        }

        DetachedExpressionImpl<? extends T> castedValueExpression = (DetachedExpressionImpl<? extends T>) valueExpression;
        if (castedValueExpression.getType() == DetachedExpressionType.LITERAL) {
          values.addAll(getValuesFromLiteralExpression(castedValueExpression));
        } else {
          throw new IllegalStateException(String.format("Unsupported value expression type %s", castedValueExpression.getType()));
        }
      }
    }

    return List.copyOf(values);
  }

  private Collection<? extends T> getValuesFromParameterExpression(
      DetachedParameterExpressionImpl<?> parameterExpression,
      Map<String, Object> parameters
  ) {
    Object parameterValue = parameters.get(parameterExpression.getName());
    if (parameterValue == null) {
      throw new IllegalStateException(String.format("No value provided for parameter %s", parameterExpression.getName()));
    }

    if (parameterValue instanceof Collection) {
      //noinspection unchecked
      return (Collection<? extends T>) parameterValue;
    }

    //noinspection unchecked
    return List.of((T) parameterValue);
  }

  private Collection<? extends T> getValuesFromLiteralExpression(DetachedExpressionImpl<? extends T> literalExpression) {
    Object value = literalExpression.getDirectArguments().get(0);
    if (value instanceof Collection) {
      //noinspection unchecked
      return (Collection<? extends T>) value;
    } else if (isObjectAllowedInCollection(value)) {
      //noinspection unchecked
      return List.of((T) value);
    } else {
      throw new IllegalStateException(String.format("Value of type %s can't be added to the collection", value.getClass()));
    }
  }

  private static boolean isObjectAllowedInCollection(Object value) {
    if (value instanceof Temporal || value instanceof String) {
      return true;
    }

    Class<?> valueClass = value.getClass();
    return valueClass.isPrimitive();
  }
}

