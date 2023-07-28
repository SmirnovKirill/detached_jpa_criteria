package kirill.detachedjpacriteria.expression.api;

import java.util.Collection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;

/**
 * Detached версия класса javax.persistence.criteria.Expression.
 * @see Expression
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedExpression<T> {
  /**
   * @see Expression#isNull()
   */
  DetachedPredicate isNull();

  /**
   * @see Expression#isNotNull() ()
   */
  DetachedPredicate isNotNull();

  /**
   * @see Expression#in(Object...)
   */
  DetachedPredicate in(Object... values);

  /**
   * @see Expression#in(Expression[])
   */
  DetachedPredicate in(DetachedExpression<?>... values);

  /**
   * @see Expression#in(Collection)
   */
  DetachedPredicate in(Collection<?> values);

  /**
   * @see Expression#in(Expression)
   */
  DetachedPredicate in(DetachedExpression<Collection<?>> values);

  /**
   * @see Selection#alias(String)
   */
  DetachedExpression<T> alias(String name);

  /**
   * @see Expression#as(Class)
   */
  <X> DetachedExpression<X> as(Class<X> type);
}
