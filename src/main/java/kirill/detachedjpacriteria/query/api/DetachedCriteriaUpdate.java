package kirill.detachedjpacriteria.query.api;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaUpdate;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPath;

/**
 * Интерфейс является аналогом интерфейса CriteriaUpdate.
 * Реализации не являются thread-safe.
 * @see CriteriaUpdate
 */
public interface DetachedCriteriaUpdate<T> extends DetachedCommonCriteria<DetachedCriteriaUpdate<T>, CriteriaUpdate<T>, Query> {
  <Y, X extends Y> DetachedCriteriaUpdate<T> set(DetachedPath<Y> attribute, X value);

  <Y> DetachedCriteriaUpdate<T> set(DetachedPath<Y> attribute, DetachedExpression<? extends Y> value);

  DetachedCriteriaUpdate<T> set(String attributeName, Object value);
}
