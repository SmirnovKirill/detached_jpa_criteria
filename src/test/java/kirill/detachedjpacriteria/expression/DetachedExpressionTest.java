package kirill.detachedjpacriteria.expression;

import java.util.List;
import java.util.Map;
import javax.persistence.Tuple;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.impl.DetachedExpressionImpl;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kirill.detachedjpacriteria.entity.UserDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.and;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.coalesce;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.concat;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.gt;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.multiselect;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.or;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.select;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectCase;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.sum;

public class DetachedExpressionTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testDeepCount() {
    DetachedExpression<?> expression = DetachedCriteriaBuilder.and(
        DetachedCriteriaBuilder.equal(DetachedCriteriaBuilder.path("title"), "test"),
        DetachedCriteriaBuilder.gt(DetachedCriteriaBuilder.sum(DetachedCriteriaBuilder.id(), 55), 60),
        DetachedCriteriaBuilder.isNotNull(DetachedCriteriaBuilder.coalesce().value(DetachedCriteriaBuilder.literal(55)).value(DetachedCriteriaBuilder.coalesce().value(
            DetachedCriteriaBuilder.path("int"))))
    );

    /*
     * 1. Внешний and
     * 2. equal
     * 3. path внутри equal
     * 4. gt
     * 5. sum внутри gt
     * 6. id внутри sum внутри gt
     * 7. isNotNull
     * 8. coalesce внутри isNotNull
     * 9. literal внутри coalesce внутри isNotNull
     * 10. coalesce внутри coalesce внутри isNotNull
     * 11. path внутри coalesce внутри coalesce внутри isNotNull
     */

    DetachedExpressionImpl<?> casterExpression = (DetachedExpressionImpl<?>) expression;
    assertEquals(11, casterExpression.getAllExpressionsDeep().size());
  }

  @Test
  public void testIn() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = DetachedCriteriaBuilder.selectEntity(UserDb.class)
        .where(
            DetachedCriteriaBuilder.or(
                DetachedCriteriaBuilder.path("name").in("name-1", "name-2", "name-5"),
                DetachedCriteriaBuilder.path("name").in("name-4", "name-8", "name-9")
            )
        );
    assertEquals(6, getResultList(criteriaQuery).size());

    criteriaQuery = DetachedCriteriaBuilder.selectEntity(UserDb.class)
        .where(
            DetachedCriteriaBuilder.and(
                DetachedCriteriaBuilder.path("name").in(DetachedCriteriaBuilder.concat(DetachedCriteriaBuilder.literal("name-"), "1"), DetachedCriteriaBuilder.literal("name-2")),
                DetachedCriteriaBuilder.path("name").in("name-3", "name-2")
            )
        );
    assertEquals(1, getResultList(criteriaQuery).size());

    criteriaQuery = DetachedCriteriaBuilder.selectEntity(UserDb.class)
        .where(
            DetachedCriteriaBuilder.and(
                DetachedCriteriaBuilder.path("name").in(List.of("name-1", "name-2", "name-3")),
                DetachedCriteriaBuilder.path("name").in(List.of("name-3", "name-4", "name-5"))
            )
        );
    assertEquals(1, getResultList(criteriaQuery).size());

    criteriaQuery = DetachedCriteriaBuilder.selectEntity(UserDb.class)
        .where(
            DetachedCriteriaBuilder.and(
                DetachedCriteriaBuilder.path("name").in(DetachedCriteriaBuilder.values(Map.of(1, "name-1", 2, "name-2", 3, "name-3"))),
                DetachedCriteriaBuilder.path("name").in(DetachedCriteriaBuilder.values(Map.of(1, "name-3", 2, "name-4", 3, "name-5")))
            )
        );
    assertEquals(1, getResultList(criteriaQuery).size());
  }

  @Test
  public void testNativeFunction() {
    Tuple tuple = readInTransaction(entityManager ->
        DetachedCriteriaBuilder.multiselect(DetachedCriteriaBuilder.isTrue(DetachedCriteriaBuilder.function("bool_or", Boolean.class, DetachedCriteriaBuilder.path("active"))))
            .intoTuple()
            .from(UserDb.class)
            .createJpaQuery(entityManager).getSingleResult()
    );
    assertTrue(tuple.get(0, Boolean.class));
  }

  @Test
  public void testAs() {
    Boolean result = readInTransaction(entityManager ->
        DetachedCriteriaBuilder.select(
            DetachedCriteriaBuilder.function(
                "bool_or",
                Object.class,
                DetachedCriteriaBuilder.selectCase().when(DetachedCriteriaBuilder.equal(DetachedCriteriaBuilder.path("active"), DetachedCriteriaBuilder.parameter("active")), true).otherwise(false)
            ).as(Boolean.class)
        )
            .into(Boolean.class)
            .from(UserDb.class)
            .parameter("active", false)
            .createJpaQuery(entityManager).getSingleResult()
    );
    assertTrue(result);
  }

  // Ради in выражения пришлось аж целый класс специальный делать (DetachedInNotTypeSafeImpl), так что надо обязательно проверить.
  @Test
  public void testAsForIn() {
    List<Tuple> result = readInTransaction(entityManager ->
        DetachedCriteriaBuilder.multiselect(
            DetachedCriteriaBuilder.path("email").in(List.of("user-3@mail.com", "user-7@mail.com", "user-666@mail.com"))
                .alias("result")
                .as(Boolean.class)
        )
            .intoTuple()
            .from(UserDb.class)
            .createJpaQuery(entityManager).getResultList()
    );
    assertEquals(2, result.stream().filter(tuple -> tuple.get("result", Boolean.class)).count());
    assertEquals(8, result.stream().filter(tuple -> !tuple.get("result", Boolean.class)).count());
  }
}
