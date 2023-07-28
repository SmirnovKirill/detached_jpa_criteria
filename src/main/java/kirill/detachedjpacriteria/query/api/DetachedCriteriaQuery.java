package kirill.detachedjpacriteria.query.api;

import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;
import kirill.detachedjpacriteria.query.Fetch;
import kirill.detachedjpacriteria.query.Join;

/**
 * Интерфейс является аналогом интерфейса CriteriaQuery.
 * Реализации не являются thread-safe.
 * @see CriteriaQuery
 */
public interface DetachedCriteriaQuery<T> extends DetachedCommonCriteria<DetachedCriteriaQuery<T>, CriteriaQuery<T>, TypedQuery<T>>,
    DetachedCommonCriteriaQuery<T> {
  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCriteriaQuery<T> where(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> where(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> where(Iterable<DetachedPredicate> predicates);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> groupBy(DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные выражения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> groupBy(List<DetachedExpression<?>> expressions);

  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  DetachedCriteriaQuery<T> having(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> having(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> having(Iterable<DetachedPredicate> predicates);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderByAsc(DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderByAsc(List<DetachedExpression<?>> expressions);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderByDesc(DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderByDesc(List<DetachedExpression<?>> expressions);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderBy(OrderBy orderBy, DetachedExpression<?>... expressions);

  /**
   * Здесь логика не как у JPA - переданные сортировки добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> orderBy(OrderBy orderBy, List<DetachedExpression<?>> expressions);

  DetachedCriteriaQuery<T> distinct(boolean distinct);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные фетчи добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> innerFetch(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные фетчи добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> leftFetch(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные фетчи добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> fetch(Fetch... fetches);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные фетчи добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> fetch(Iterable<Fetch> fetches);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> innerJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> leftJoin(String... attributeNames);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> join(Join... joins);

  /**
   * Здесь логика такая же как в аналогичных местах и не как у JPA - переданные джоины добавляются к имеющимся ранее, а не перезаписывают их.
   */
  DetachedCriteriaQuery<T> join(Iterable<Join> joins);

  interface FirstStepQueryBuilderSingleWithType<T> {
    SecondStepQueryBuilder<T> into(Class<T> resultClass);
  }

  interface FirstStepQueryBuilderSingleWithoutType {
    <T> SecondStepQueryBuilder<T> into(Class<T> resultClass);
  }

  interface FirstStepQueryBuilderMulti {
    <T> SecondStepQueryBuilder<T> into(Class<T> resultClass);

    SecondStepQueryBuilder<Tuple> intoTuple();
  }

  interface SecondStepQueryBuilder<T> {
    DetachedCriteriaQuery<T> from(Class<?> fromClass);
  }

  enum OrderBy {
    ASC,
    DESC
  }
}
