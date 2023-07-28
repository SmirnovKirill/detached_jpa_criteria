package kirill.detachedjpacriteria.expression;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.entity.UserDb;
import kirill.detachedjpacriteria.entity.UserType;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.coalesce;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.multiselect;

public class HibernateImplAndLibErrorCornerCasesTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  /*
   * Тут уже не просто bool_or, а еще и coalesce, тест показывает что с реализацей JPA Хибером такие же проблемы.
   */
  @Test
  public void testCoalesceWithNativeFunctionDoesNotWork() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> readInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
          Root<?> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.multiselect(
              criteriaBuilder.coalesce()
                  .value(criteriaBuilder.isTrue(criteriaBuilder.function("bool_or", Boolean.class, root.get("active"))))
                  .value(false)
          );

          return entityManager.createQuery(criteriaQuery).getSingleResult();
        })
    );
    assertTrue(exception.getMessage().startsWith(
        "org.hibernate.QueryException: No data type for node: org.hibernate.hql.internal.ast.tree.MethodNode")
    );
    assertTrue(exception.getMessage().contains("\\-[METHOD_CALL] MethodNode: '('\n"));

    exception = assertThrows(
        IllegalArgumentException.class,
        () -> readInTransaction(entityManager ->
            DetachedCriteriaBuilder.multiselect(
                DetachedCriteriaBuilder.coalesce()
                    .value(DetachedCriteriaBuilder.isTrue(DetachedCriteriaBuilder.function("bool_or", Boolean.class, DetachedCriteriaBuilder.path("active"))))
                    .value(false)
            )
                .intoTuple()
                .from(UserDb.class)
                .createJpaQuery(entityManager).getSingleResult()
        )
    );
    assertTrue(exception.getMessage().startsWith(
        "org.hibernate.QueryException: No data type for node: org.hibernate.hql.internal.ast.tree.MethodNode")
    );
    assertTrue(exception.getMessage().contains("\\-[METHOD_CALL] MethodNode: '('\n"));
  }

  /*
   * Чтобы обойти проблему из теста выше, будем использовать coalesce не встроенный в JPA Criteria а как бы нативный, через function.
   */
  @Test
  public void testCoalesceWithNativeFunctionWorkaround() {
    Tuple tuple = readInTransaction(entityManager ->
        DetachedCriteriaBuilder.multiselect(
            DetachedCriteriaBuilder.isTrue(
                DetachedCriteriaBuilder.function(
                    "coalesce",
                    Boolean.class,
                    DetachedCriteriaBuilder.function("bool_or", Boolean.class, DetachedCriteriaBuilder.path("active")),
                    DetachedCriteriaBuilder.literal(false)
                )
            )
        )
            .intoTuple()
            .from(UserDb.class)
            .createJpaQuery(entityManager).getSingleResult()
    );
    assertTrue(tuple.get(0, Boolean.class));
  }

  /*
   * Тут проблема в том что Хибер не понимает что глубоко внутри селекта есть параметр.
   */
  @Test
  public void testNamedParameterInNativeFunction() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
          Root<?> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.multiselect(
              criteriaBuilder.isTrue(
                  criteriaBuilder.function(
                      "coalesce",
                      Boolean.class,
                      criteriaBuilder.function(
                          "bool_or",
                          Boolean.class,
                          criteriaBuilder.equal(root.get("login"), criteriaBuilder.parameter(String.class, "param"))
                      )
                  )
              )
          );
          entityManager.createQuery(criteriaQuery)
              .setParameter("param", "test")
              .getSingleResult();
        })
    );
    assertEquals("Could not locate named parameter [param], expecting one of []", exception.getMessage());

    exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager ->
            DetachedCriteriaBuilder.multiselect(
                DetachedCriteriaBuilder.isTrue(
                    DetachedCriteriaBuilder.function(
                        "coalesce",
                        Boolean.class,
                        DetachedCriteriaBuilder.function(
                            "bool_or",
                            Boolean.class,
                            DetachedCriteriaBuilder.equal(DetachedCriteriaBuilder.path("login"), DetachedCriteriaBuilder.parameter("param"))
                        ),
                        DetachedCriteriaBuilder.literal(false)
                    )
                )
            )
                .intoTuple()
                .from(UserDb.class)
                .parameter("param", "test")
                .createJpaQuery(entityManager).getSingleResult()
        )
    );
    assertEquals("Could not locate named parameter [param], expecting one of []", exception.getMessage());
  }

  /*
   * Нельзя просто передать значение, создается параметр с именем param0, то есть implicit binding. А не работает это опять же потому что параметр
   * в сложном запросе и его там не видно.
   */
  @Test
  public void testComplexExpressionsCreateParameters() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
          Root<UserDb> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.multiselect(
              criteriaBuilder.isTrue(
                  criteriaBuilder.function(
                      "bool_or",
                      Boolean.class,
                      criteriaBuilder.equal(root.get("userTypeStr"), UserType.ADMINISTRATOR)
                  )
              )
          );
          entityManager.createQuery(criteriaQuery).getResultList();
        })
    );
    assertEquals("Could not locate named parameter [param0], expecting one of []", exception.getMessage());

    exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager -> DetachedCriteriaBuilder.multiselect(
            DetachedCriteriaBuilder.isTrue(
                DetachedCriteriaBuilder.function(
                    "bool_or",
                    Boolean.class,
                    DetachedCriteriaBuilder.equal(DetachedCriteriaBuilder.path("userTypeStr"), UserType.ADMINISTRATOR)
                )
            )
        ).intoTuple().from(UserDb.class).createJpaQuery(entityManager).getResultList())
    );
    assertEquals("Could not locate named parameter [param0], expecting one of []", exception.getMessage());
  }
}
