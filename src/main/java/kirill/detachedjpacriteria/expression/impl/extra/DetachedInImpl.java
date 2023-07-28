package kirill.detachedjpacriteria.expression.impl.extra;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.extra.DetachedIn;

public class DetachedInImpl<T> extends DetachedInNotTypeSafeImpl<T, Boolean> implements DetachedIn<T> {
  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedInImpl(DetachedExpression<? extends T> expression) {
    super(List.of(expression), null, null, new CopyOnWriteArrayList<>());
  }

  @Override
  public DetachedIn<T> value(T value) {
    values.add(DetachedCriteriaBuilder.literal(value));
    return this;
  }

  @Override
  public DetachedIn<T> value(DetachedExpression<? extends T> value) {
    values.add(value);
    return this;
  }
}
