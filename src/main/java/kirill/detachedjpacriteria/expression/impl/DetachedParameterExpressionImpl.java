package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedParameterExpression;

public class DetachedParameterExpressionImpl<T> extends DetachedExpressionImpl<T> implements DetachedParameterExpression<T> {
  private final String name;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedParameterExpressionImpl(String name) {
    super(DetachedExpressionType.PARAMETER, List.of());
    this.name = name;
  }

  private DetachedParameterExpressionImpl(List<?> directArguments, String alias, Class<T> asClass, String name) {
    super(DetachedExpressionType.PARAMETER, directArguments, alias, asClass);
    this.name = name;
  }

  @Override
  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedParameterExpressionImpl<>(directArguments, alias, type, name);
  }

  public String getName() {
    return name;
  }
}
