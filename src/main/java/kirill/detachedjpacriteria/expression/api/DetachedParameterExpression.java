package kirill.detachedjpacriteria.expression.api;

import javax.persistence.criteria.ParameterExpression;

/**
 * Detached версия класса javax.persistence.criteria.ParameterExpression.
 * @see ParameterExpression
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedParameterExpression<T> extends DetachedExpression<T> {
}
