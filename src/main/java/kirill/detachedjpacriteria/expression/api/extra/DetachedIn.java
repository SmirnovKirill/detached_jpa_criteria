package kirill.detachedjpacriteria.expression.api.extra;

import javax.persistence.criteria.Expression;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;

/**
 * Detached версия класса javax.persistence.criteria.CriteriaBuilder.In.
 * @see javax.persistence.criteria.CriteriaBuilder.In
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedIn<T> extends DetachedPredicate {
  /**
   * @see javax.persistence.criteria.CriteriaBuilder.In#value(Object)
   */
  DetachedIn<T> value(T value);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.In#value(Expression)
   */
  DetachedIn<T> value(DetachedExpression<? extends T> value);
}
