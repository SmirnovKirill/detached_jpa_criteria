package kirill.detachedjpacriteria.query.api;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CommonAbstractCriteria;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;

/**
 * Интерфейс является аналогом интерфейса CommonAbstractCriteria, и хотя на деле ни один метод оттуда не совпадает с методами в этом интерфейсе,
 * сходство есть в плане иерархии.
 * Реализации не являются thread-safe.
 * @see CommonAbstractCriteria
 */
public interface DetachedCommonCriteria<T extends DetachedCommonCriteria<T, C, Q>, C extends CommonAbstractCriteria, Q extends Query> {
  /**
   * Здесь логика не как у JPA - переданное выражение добавляется к имеющимся ранее, а не перезаписывает их.
   */
  T where(DetachedExpression<Boolean> expression);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  T where(DetachedPredicate... predicates);

  /**
   * Здесь логика не как у JPA - переданные ограничения добавляются к имеющимся ранее, а не перезаписывают их.
   */
  T where(Iterable<DetachedPredicate> predicates);

  /**
   * Передаваемое значение параметра обязательно должно быть не null, иначе не будет возможности узнать класс параметра (который нужен JPA).
   * Для параметров чьи значения могут быть null следует использовать метод nullableParameter(String, Object, Class).
   *
   * @see DetachedCommonCriteria#nullableParameter(String, Object, Class)
   */
  <X> T parameter(String name, X value);

  /**
   * Передаваемое значение параметра может быть null, однако если известно, что null никогда не бывает, удобнее использовать метод
   * parameter(String, Object) потому что он требует меньше аргументов.
   *
   * @see DetachedCommonCriteria#parameter(String, Object)
   */
  <X> T nullableParameter(String name, X value, Class<X> valueClass);

  /**
   * Возвращает объект-наследник CommonAbstractCriteria в зависимости от текущего запроса. В целом удобнее работать с результатом метода
   * {@link #createJpaQuery(EntityManager)}, потому что Query уже можно передать в EntityManager и выполнить запрос, но этот метод дает большую
   * гранулярность на случай если нужно больше контроля.
   */
  C createJpaCriteriaQuery(EntityManager entityManager);

  /**
   * Возвращает объект-наследник Query в зависимости от текущего запроса. Это более удобный метод чем {@link #createJpaCriteriaQuery(EntityManager)},
   * потому что результат уже можно сразу передать в EntityManager и выполнить запрос, а тот метод нужен для большей гранулярности и контроля.
   */
  Q createJpaQuery(EntityManager entityManager);

  DetachedCriteriaQuery<Long> toCountCriteriaQuery(DetachedExpression<?> countExpression);

  DetachedCriteriaQuery<Long> toCountDistinctCriteriaQuery(DetachedExpression<?> countExpression);

  /**
   * Копирует в текущий запрос из другого запроса указанные части.
   */
  void copyFromOtherCriteria(DetachedCommonCriteria<?, ?, ?> otherCriteria, QueryCopyPart... copyParts);
}
