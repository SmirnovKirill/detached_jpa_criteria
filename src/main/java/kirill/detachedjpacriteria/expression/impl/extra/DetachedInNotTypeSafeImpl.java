package kirill.detachedjpacriteria.expression.impl.extra;

import java.util.ArrayList;
import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionType;

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
}

