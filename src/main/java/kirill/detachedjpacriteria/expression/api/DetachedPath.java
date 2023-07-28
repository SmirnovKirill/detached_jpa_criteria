package kirill.detachedjpacriteria.expression.api;

import javax.persistence.criteria.Path;

/**
 * Detached версия класса javax.persistence.criteria.Path.
 * @see Path
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedPath<X> extends DetachedExpression<X> {
  /**
   * @see Path#type()
   */
  DetachedExpression<Class<? extends X>> type();
}
