package kirill.detachedjpacriteria.expression.api.extra;

import javax.persistence.criteria.Expression;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;

/**
 * Detached версия класса javax.persistence.criteria.CriteriaBuilder.Coalesce.
 * @see javax.persistence.criteria.CriteriaBuilder.Coalesce
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedCoalesce<T> extends DetachedExpression<T> {
  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Coalesce#value(Object)
   */
  DetachedCoalesce<T> value(T value);

  /**
   * @see javax.persistence.criteria.CriteriaBuilder.Coalesce#value(Expression)
   */
  DetachedCoalesce<T> value(DetachedExpression<? extends T> value);
}
