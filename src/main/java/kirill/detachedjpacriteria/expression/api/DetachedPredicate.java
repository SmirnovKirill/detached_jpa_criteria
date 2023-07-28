package kirill.detachedjpacriteria.expression.api;

import javax.persistence.criteria.Predicate;

/**
 * Detached версия класса javax.persistence.criteria.Predicate.
 * @see Predicate
 *
 * Имплементации должны быть thread-safe
 */
public interface DetachedPredicate extends DetachedExpression<Boolean> {
}
