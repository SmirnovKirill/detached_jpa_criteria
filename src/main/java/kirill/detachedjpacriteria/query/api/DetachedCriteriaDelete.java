package kirill.detachedjpacriteria.query.api;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaDelete;

/**
 * Интерфейс является аналогом интерфейса CriteriaDelete.
 * Реализации не являются thread-safe.
 * @see CriteriaDelete
 */
public interface DetachedCriteriaDelete<T> extends DetachedCommonCriteria<DetachedCriteriaDelete<T>, CriteriaDelete<T>, Query> {
}
