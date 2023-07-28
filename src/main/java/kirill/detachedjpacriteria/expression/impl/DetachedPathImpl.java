package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPath;

public class DetachedPathImpl<T> extends DetachedExpressionImpl<T> implements DetachedPath<T> {
  private final Mode mode;
  private final AttributePath attributePath;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedPathImpl(Mode mode, String... attributeNames) {
    super(DetachedExpressionType.PATH, List.of());
    this.mode = mode;
    this.attributePath = AttributePath.path(attributeNames);
  }

  private DetachedPathImpl(
      List<?> directArguments,
      String alias,
      Class<T> asClass,
      Mode mode,
      AttributePath attributePath
  ) {
    super(DetachedExpressionType.PATH, directArguments, alias, asClass);
    this.mode = mode;
    this.attributePath = attributePath;
  }

  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedPathImpl<>(directArguments, alias, type, mode, attributePath);
  }

  @Override
  public DetachedExpression<Class<? extends T>> type() {
    return new DetachedExpressionImpl<>(DetachedExpressionType.TYPE, List.of(this));
  }

  public Mode getMode() {
    return mode;
  }

  public AttributePath getAttributePath() {
    return attributePath;
  }

  public enum Mode {
    ID,
    ROOT,
    PATH,
    PARENT_ID,
    PARENT_ROOT,
    PARENT_PATH
  }
}
