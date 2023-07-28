package kirill.detachedjpacriteria.query;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import kirill.detachedjpacriteria.AbstractTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kirill.detachedjpacriteria.entity.UserDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.and;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.concat;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.id;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.in;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.isNotNull;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.isTrue;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.literal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.not;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.values;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;

public class CriteriaQueryBatchesTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testIncorrectQueryInStatementInGroupBy() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(id().in(List.of(1, 2, 3, 4, 5)))
        .groupBy(id().in(List.of(1, 2, 3, 4, 5)));

    testIncorrectQuery(detachedQuery, "IN expression is not allowed inside group by");
  }

  @Test
  public void testIncorrectQueryInStatementInHaving() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(id().in(List.of(1, 2, 3, 4, 5)))
        .groupBy(id())
        .having(isTrue(id().in(List.of(1, 2, 3, 4, 5))));

    testIncorrectQuery(detachedQuery, "IN expression is not allowed inside having");
  }

  @Test
  public void testIncorrectQueryInStatementInOrder() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(id().in(List.of(1, 2, 3, 4, 5)))
        .orderByAsc(id().in(List.of(1, 2, 3, 4, 5)));

    testIncorrectQuery(detachedQuery, "IN expression is not allowed inside order by");

    detachedQuery = selectEntity(UserDb.class)
        .where(id().in(List.of(1, 2, 3, 4, 5)))
        .orderByDesc(id().in(List.of(1, 2, 3, 4, 5)));

    testIncorrectQuery(detachedQuery, "IN expression is not allowed inside order by");
  }

  @Test
  public void testIncorrectQueryMoreThanOneStatement() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            and(
                id().in(List.of(1, 2, 3, 4, 5)),
                in(id()).value(id().in(5, 4, 3, 2, 1))
            )
        );

    testIncorrectQuery(detachedQuery, "Should be exactly one IN expression, got 3");
  }

  @Test
  public void testIncorrectQueryNotRootPredicate() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(not(id().in(List.of(1, 2, 3, 4, 5))));

    testIncorrectQuery(detachedQuery, "IN expression should be one of the root expressions");
  }

  /* Тут технически запрос нормальный, конъюнкция в конъюнкции, но для упрощения проверок мы такое делать не даем. Этот запрос легко переписать. */
  @Test
  public void testIncorrectQueryNotRootPredicateAlthoughCorrect() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            and(
                id().in(List.of(1, 2, 3, 4, 5)),
                isNotNull(id())
            )
        );

    testIncorrectQuery(detachedQuery, "IN expression should be one of the root expressions");
  }

  @Test
  public void testIncorrectQueryNotSimpleValues() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(id().in(values(Map.of("key", 1))));

    testIncorrectQuery(detachedQuery, "IN expression values are not simple (collections, primitives, dates, nulls)");
  }

  @Test
  public void testSelectWithBatchesCollection() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            concat(path("name"), concat(path("name"), "111")).in(
                List.of("name-1name-1111", "name-3name-3111", "name-5name-5111", "name-2name-2111", "name-9name-9111")
            )
        );
    List<UserDb> users = getResultListWithBatches(detachedQuery);
    assertEquals(5, users.size());
  }

  @Test
  public void testSelectWithBatchesArray() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            concat(path("name"), concat(path("name"), "111")).in(
                "name-1name-1111", "name-3name-3111", "name-5name-5111", "name-2name-2111", "name-9name-9111"
            )
        );
    List<UserDb> users = getResultListWithBatches(detachedQuery);
    assertEquals(5, users.size());
  }

  @Test
  public void testSelectWithBatchesLiteral() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            concat(path("name"), concat(path("name"), "111")).in(
                literal(List.of("name-1name-1111", "name-3name-3111", "name-5name-5111", "name-2name-2111", "name-9name-9111"))
            )
        );
    List<UserDb> users = getResultListWithBatches(detachedQuery);
    assertEquals(5, users.size());
  }

  @Test
  public void testSelectWithBatchesInverse() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(
            in(concat(path("name"), concat(path("name"), "111")))
                .value(literal("name-1name-1111"))
                .value("name-3name-3111")
                .value(literal("name-5name-5111"))
                .value("name-2name-2111")
                .value("name-9name-9111")
        );
    List<UserDb> users = getResultListWithBatches(detachedQuery);
    assertEquals(5, users.size());
  }

  private void testIncorrectQuery(DetachedCriteriaQuery<UserDb> detachedQuery, String expectedMessage) {
    txScope.doInTransaction(() -> {
      EntityManager entityManager = sessionFactory.getCurrentSession();

      IllegalStateException exception = assertThrows(
          IllegalStateException.class,
          () -> detachedQuery.createJpaBatchQueries(entityManager, 2)
      );
      assertEquals(expectedMessage, exception.getMessage());
    });
  }
}
