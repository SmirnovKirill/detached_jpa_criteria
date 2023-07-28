package kirill.detachedjpacriteria.query.api;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.query.Join;

/**
 * Интерфейс является аналогом интерфейса Subquery.
 * Реализации не являются thread-safe.
 * @see javax.persistence.criteria.Subquery
 */
public interface DetachedCriteriaSubquery<T> extends DetachedCommonCriteriaQuery<T>, DetachedExpression<T> {
  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCriteriaSubquery<T> where(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> where(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> where(Iterable<DetachedPredicate> predicates);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> groupBy(DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> groupBy(List<DetachedExpression<?>> expressions);

  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCriteriaSubquery<T> having(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> having(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> having(Iterable<DetachedPredicate> predicates);

  DetachedCriteriaSubquery<T> distinct(boolean distinct);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> innerJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> leftJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> join(Join... joins);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaSubquery<T> join(Iterable<Join> joins);

  /**
   * Копирует в текущий запрос из другого запроса указанные части.
   */
  void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts);

  interface FirstStepQueryBuilderWithType<T> {
    SecondStepQueryBuilder<T> into(Class<T> resultClass);
  }

  interface FirstStepQueryBuilderWithoutType {
    <T> SecondStepQueryBuilder<T> into(Class<T> resultClass);
  }

  interface SecondStepQueryBuilder<T> {
    DetachedCriteriaSubquery<T> from(Class<?> fromClass);
  }
}
