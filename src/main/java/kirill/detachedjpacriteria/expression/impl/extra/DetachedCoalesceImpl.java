package kirill.detachedjpacriteria.expression.impl.extra;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.literal;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.extra.DetachedCoalesce;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionType;

public class DetachedCoalesceImpl<T> extends DetachedExpressionImpl<T> implements DetachedCoalesce<T> {
  private final List<DetachedExpression<?>> values;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedCoalesceImpl() {
    super(DetachedExpressionType.COALESCE, List.of());
    this.values = new CopyOnWriteArrayList<>();
  }

  private DetachedCoalesceImpl(
      List<?> directArguments,
      String alias,
      Class<T> asClass,
      List<DetachedExpression<?>> values
  ) {
    super(DetachedExpressionType.COALESCE, directArguments, alias, asClass);
    this.values = values;
  }

  @Override
  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedCoalesceImpl<>(directArguments, alias, type, values);
  }

  @Override
  public DetachedCoalesce<T> value(T value) {
    values.add(literal(value));
    return this;
  }

  @Override
  public DetachedCoalesce<T> value(DetachedExpression<? extends T> value) {
    values.add(value);
    return this;
  }

  public List<DetachedExpression<?>> getValues() {
    return values;
  }

  @Override
  public List<?> getExtraArguments() {
    return new ArrayList<>(values);
  }
}
