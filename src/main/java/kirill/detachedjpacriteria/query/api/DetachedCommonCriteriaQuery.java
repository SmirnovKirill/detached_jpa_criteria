package kirill.detachedjpacriteria.query.api;

import java.util.List;
import javax.persistence.criteria.AbstractQuery;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.query.Join;

/**
 * Интерфейс является аналогом интерфейса AbstractQuery.
 * Реализации не являются thread-safe.
 * @see AbstractQuery
 */
public interface DetachedCommonCriteriaQuery<T> {
  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCommonCriteriaQuery<T> where(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> where(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> where(Iterable<DetachedPredicate> predicates);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> groupBy(DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> groupBy(List<DetachedExpression<?>> expressions);

  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCommonCriteriaQuery<T> having(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> having(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> having(Iterable<DetachedPredicate> predicates);

  DetachedCommonCriteriaQuery<T> distinct(boolean distinct);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> innerJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> leftJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> join(Join... joins);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCommonCriteriaQuery<T> join(Iterable<Join> joins);
}
