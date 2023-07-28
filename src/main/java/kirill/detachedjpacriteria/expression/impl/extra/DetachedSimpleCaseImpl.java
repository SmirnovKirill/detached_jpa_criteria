package kirill.detachedjpacriteria.expression.impl.extra;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionType;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.extra.DetachedSimpleCase;

public class DetachedSimpleCaseImpl<C, R> extends DetachedExpressionImpl<R> implements DetachedSimpleCase<C, R> {
  private final List<When<?, ?>> whenList;
  private final List<DetachedExpression<?>> otherwiseList;

  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedSimpleCaseImpl(DetachedExpression<? extends C> expression) {
    super(DetachedExpressionType.SELECT_CASE, List.of(expression));
    this.whenList = new CopyOnWriteArrayList<>();
    this.otherwiseList = new CopyOnWriteArrayList<>();
  }

  private DetachedSimpleCaseImpl(
      List<?> directArguments,
      String alias,
      Class<R> asClass,
      List<When<?, ?>> whenList,
      List<DetachedExpression<?>> otherwiseList
  ) {
    super(DetachedExpressionType.SELECT_CASE, directArguments, alias, asClass);
    this.whenList = whenList;
    this.otherwiseList = otherwiseList;
  }

  @Override
  public <X> DetachedExpression<X> as(Class<X> type) {
    return new DetachedSimpleCaseImpl<>(directArguments, alias, type, whenList, otherwiseList);
  }

  @Override
  public DetachedSimpleCase<C, R> when(C condition, R result) {
    whenList.add(new When<>(condition, DetachedCriteriaBuilder.literal(result)));
    return this;
  }

  @Override
  public DetachedSimpleCase<C, R> when(C condition, DetachedExpression<? extends R> result) {
    whenList.add(new When<>(condition, result));
    return this;
  }

  @Override
  public DetachedExpression<R> otherwise(R result) {
    otherwiseList.add(DetachedCriteriaBuilder.literal(result));
    return this;
  }

  @Override
  public DetachedExpression<R> otherwise(DetachedExpression<? extends R> result) {
    otherwiseList.add(result);
    return this;
  }

  public List<When<?, ?>> getWhenList() {
    return whenList;
  }

  public List<DetachedExpression<?>> getOtherwiseList() {
    return otherwiseList;
  }

  @Override
  public List<?> getExtraArguments() {
    List<Object> result = new ArrayList<>();

    for (When<?, ?> when : whenList) {
      result.add(when.getCondition());
      result.add(when.getResult());
    }
    result.addAll(otherwiseList);

    return result;
  }

  /**
   * Класс иммутабельный и поэтому thread-safe.
   */
  public static class When<C, R> {
    private final C condition;
    private final DetachedExpression<R> result;

    public When(C condition, DetachedExpression<R> result) {
      this.condition = condition;
      this.result = result;
    }

    public C getCondition() {
      return condition;
    }

    public DetachedExpression<R> getResult() {
      return result;
    }
  }
}
