package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;

public class DetachedPredicateImpl extends DetachedExpressionImpl<Boolean> implements DetachedPredicate {
  /**
   * Пользователи библиотеки не должны пользоваться этим конструктором, нужно использовать DetachedCriteriaBuilder.
   */
  public DetachedPredicateImpl(DetachedExpressionType type, List<?> arguments) {
    super(type, arguments);
  }

  protected DetachedPredicateImpl(DetachedExpressionType type, List<?> directArguments, String alias, Class<Boolean> asClass) {
    super(type, directArguments, alias, asClass);
  }
}
