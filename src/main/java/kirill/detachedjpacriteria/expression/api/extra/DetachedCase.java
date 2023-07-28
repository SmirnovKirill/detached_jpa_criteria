package kirill.detachedjpacriteria.expression.api.extra;

import javax.persistence.criteria.Expression;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;

/**
 * Detached версия класса javax.persistence.criteria.CriteriaBuilder.Case.
 * @see javax.persistence.criteria.CriteriaBuilder.Case
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedCase<R> extends DetachedExpression<R> {
  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Case#when(Expression, Object)
   */
  DetachedCase<R> when(DetachedExpression<Boolean> condition, R result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Case#when(Expression, Expression)
   */
  DetachedCase<R> when(DetachedExpression<Boolean> condition, DetachedExpression<? extends R> result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Case#otherwise(Object)
   */
  DetachedExpression<R> otherwise(R result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Case#otherwise(Expression)
   */
  DetachedExpression<R> otherwise(DetachedExpression<? extends R> result);
}
