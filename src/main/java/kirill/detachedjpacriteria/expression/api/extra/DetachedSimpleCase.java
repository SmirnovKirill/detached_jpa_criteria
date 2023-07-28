package kirill.detachedjpacriteria.expression.api.extra;

import javax.persistence.criteria.Expression;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;

/**
 * Detached версия класса javax.persistence.criteria.CriteriaBuilder.SimpleCase.
 * @see javax.persistence.criteria.CriteriaBuilder.SimpleCase
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedSimpleCase<C, R> extends DetachedExpression<R> {
  /**
   * @see javax.persistence.criteria.CriteriaBuilder.SimpleCase#when(Object, Object)
   */
  DetachedSimpleCase<C, R> when(C condition, R result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.SimpleCase#when(Object, Expression)
   */
  DetachedSimpleCase<C, R> when(C condition, DetachedExpression<? extends R> result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.SimpleCase#otherwise(Object)
   */
  DetachedExpression<R> otherwise(R result);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.SimpleCase#otherwise(Expression)
   */
  DetachedExpression<R> otherwise(DetachedExpression<? extends R> result);
}
