package kirill.detachedjpacriteria.query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import kirill.detachedjpacriteria.AbstractTest;
import org.hibernate.LazyInitializationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kirill.detachedjpacriteria.entity.CommentDb;
import kirill.detachedjpacriteria.entity.UserDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.and;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.concat;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.count;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.id;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.isNotNull;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.like;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.literal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.multiselect;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parameter;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.select;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;

public class CriteriaQueryTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testSelectEntity() {
    List<UserDb> users = getResultList(selectEntity(UserDb.class));

    assertEquals(10, users.size());
  }

  @Test
  public void testSelectTuple() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        id(),
        literal(111),
        path("name"),
        concat(path("lastName"), "test")
    );
    List<Tuple> rows = getResultList(multiselect(selectExpressions).intoTuple().from(UserDb.class));
    assertEquals(10, rows.size());

    Tuple matchingRow = rows.stream().filter(row -> Objects.equals(row.get(2, String.class), "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
    assertEquals(4, matchingRow.getElements().size());
    assertEquals(111, matchingRow.get(1, Integer.class));
    assertEquals("last-name-5test", matchingRow.get(3, String.class));
  }

  @Test
  public void testSelectTupleSingleAttr() {
    List<Tuple> rows = getResultList(multiselect(id().alias("user_id")).intoTuple().from(UserDb.class));
    assertEquals(10, rows.size());

    assertTrue(rows.stream().allMatch(row -> row.get("user_id", Integer.class) > 0));
  }

  @Test
  public void testSelectString() {
    List<String> rows = getResultList(select(path("name")).into(String.class).from(UserDb.class));
    assertEquals(10, rows.size());

    String matchingRow = rows.stream().filter(row -> Objects.equals(row, "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
  }

  /*
   * Тут какая-то странная история. В апи javax.persistence.criteria.CriteriaQuery сказано: "If the type of the criteria query is CriteriaQuery<X[]>
   * for some class X, an instance of type X[] will be returned for each row. The elements of the array will correspond to the arguments of the
   * multiselect method, in the specified order.". Но на деле возвращается не String[] а Object[]. Причем в тесте как раз видно что с чистой критерией
   * ровно такое же поведение, не помогает даже вызов методов .as() для каста.
   */
  @Test
  public void testSelectStringArray() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        path("name"),
        path("lastName")
    );
    List<String[]> libraryResult = getResultList(multiselect(selectExpressions).into(String[].class).from(UserDb.class));

    assertEquals(10, libraryResult.size());
    assertThrows(ClassCastException.class, () -> libraryResult.get(0).getClass());

    List<String[]> jpaResult = readInTransaction(entityManager -> {
      CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

      CriteriaQuery<String[]> criteriaQuery = criteriaBuilder.createQuery(String[].class);
      Root<?> root = criteriaQuery.from(UserDb.class);
      criteriaQuery.multiselect(root.get("name").as(String.class), root.get("lastName").as(String.class));

      return entityManager.createQuery(criteriaQuery).getResultList();
    });

    assertEquals(10, jpaResult.size());
    assertThrows(ClassCastException.class, () -> jpaResult.get(0).getClass());
  }

  @Test
  public void testSelectObjectOneField() {
    List<Object> rows = getResultList(select(path("name")).into(Object.class).from(UserDb.class));
    assertEquals(10, rows.size());

    Object matchingRow = rows.stream().filter(row -> Objects.equals(row, "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
  }

  @Test
  public void testSelectObjectTwoFields() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        path("name"),
        path("lastName")
    );
    List<Object> rows = getResultList(multiselect(selectExpressions).into(Object.class).from(UserDb.class));
    assertEquals(10, rows.size());

    Object[] matchingRow = (Object[]) rows.stream().filter(row -> Objects.equals(((Object[]) row)[0], "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
    assertEquals("last-name-5", matchingRow[1]);
  }

  @Test
  public void testSelectObjectArray() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        path("name"),
        path("lastName")
    );
    List<Object[]> rows = getResultList(multiselect(selectExpressions).into(Object[].class).from(UserDb.class));
    assertEquals(10, rows.size());

    Object[] matchingRow = rows.stream().filter(row -> Objects.equals(row[0], "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
    assertEquals("last-name-5", matchingRow[1]);
  }

  @Test
  public void testSelectUserDefinedClass() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        id(),
        path("login"),
        path("email")
    );
    List<UserBriefInfo> users = getResultList(multiselect(selectExpressions).into(UserBriefInfo.class).from(UserDb.class));
    assertEquals(10, users.size());

    UserBriefInfo matchingUser = users.stream().filter(user -> Objects.equals(user.getLogin(), "user-5")).findFirst().orElse(null);
    assertNotNull(matchingUser);
    assertEquals("user-5@mail.com", matchingUser.getEmail());
  }

  @Test
  public void testSelectAlias() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        literal(111).alias("check"),
        path("name"),
        path("lastName").alias("surname")
    );
    List<Tuple> rows = getResultList(
        multiselect(selectExpressions).intoTuple().from(UserDb.class).where(isNotNull(path("lastName")))
    );
    assertEquals(10, rows.size());

    Tuple matchingRow = rows.stream().filter(row -> Objects.equals(row.get(1, String.class), "name-5")).findFirst().orElse(null);
    assertNotNull(matchingRow);
    assertEquals(111, matchingRow.get("check", Integer.class));
    assertEquals("last-name-5", matchingRow.get("surname", String.class));
    assertThrows(IllegalArgumentException.class, () -> matchingRow.get("lastName", String.class));
  }

  /* Нельзя давать разные алиасы одним и тем же колонкам, тест также демонстрирует что такое же поведение и в JPA. */
  @Test
  public void testSelectTwoDifferentAliasesSameColumn() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        path("lastName").alias("surname"),
        path("lastName").alias("last_name")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> getResultList(multiselect(selectExpressions).intoTuple().from(UserDb.class)),
        "Multi-select expressions defined duplicate alias : last_name"
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> readInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
          Root<?> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.multiselect(root.get("lastName").alias("surname"), root.get("lastName").alias("last_name"));

          return entityManager.createQuery(criteriaQuery).getResultList();
        }),
        "Multi-select expressions defined duplicate alias : last_name"
    );
  }

  @Test
  public void testCountEntityWithoutFetch() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class).where(path("name").in("name-1", "name-2", "name-3"));
    long count = getCount(criteriaQuery);
    assertEquals(3, count);
  }

  @Test
  public void testCountEntityWithFetch() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .fetch(Fetch.innerFetch("posts").addChild(Fetch.innerFetch("comments")));
    long count = getCount(criteriaQuery);
    assertEquals(500, count);
  }

  @Test
  public void testCountTuple() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        literal(111),
        path("name"),
        concat(path("lastName"), "test")
    );
    long count = getCount(multiselect(selectExpressions).intoTuple().from(UserDb.class));
    assertEquals(10, count);
  }

  @Test
  public void testSelectDistinct() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .fetch(Fetch.innerFetch("posts"));
    List<UserDb> users = getResultList(criteriaQuery);
    assertEquals(50, users.size());

    criteriaQuery.distinct(true);
    users = getResultList(criteriaQuery);
    assertEquals(10, users.size());
  }

  @Test
  public void testConditionWithDateParameter() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .where(equal(path("created"), parameter("created")))
        .parameter("created", ZonedDateTime.parse("05.01.2020 05:00:00", FORMATTER));
    List<UserDb> users = getResultList(criteriaQuery);

    assertEquals(1, users.size());
    assertEquals("name-4", users.get(0).getName());
  }

  @Test
  public void testGroupByAndHaving() {
    List<DetachedExpression<?>> selectExpressions = List.of(
        count(literal("*")).alias("userCount"),
        path("lastLogin")
    );
    DetachedCriteriaQuery<Tuple> criteriaQuery = multiselect(selectExpressions).intoTuple().from(UserDb.class)
        .groupBy(path("lastLogin"))
        .having(equal(count(literal("*")), parameter("countThreshold")))
        .parameter("countThreshold", 10L);

    Tuple result = getSingleResult(criteriaQuery);
    assertEquals(10, result.get("userCount", Long.class));
    assertNull(result.get(1, ZonedDateTime.class));
  }

  @Test
  public void testOrderByAsc() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .orderByAsc(path("created"));
    List<UserDb> users = getResultList(criteriaQuery);

    assertEquals(10, users.size());
    assertEquals("name-1", users.get(0).getName());
  }

  @Test
  public void testOrderByDesc() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .orderByDesc(path("created"));
    List<UserDb> users = getResultList(criteriaQuery);

    assertEquals(10, users.size());
    assertEquals("name-10", users.get(0).getName());
  }

  @Test
  public void testSelectById() {
    UserDb expectedUser = getSingleResult(selectEntity(UserDb.class).where(equal(path("name"), "name-1")));
    UserDb actualUser = getSingleResult(selectEntity(UserDb.class).where(equal(id(), expectedUser.getId())));
    assertNotNull(actualUser);
    assertEquals(expectedUser.getId(), actualUser.getId());
  }

  @Test
  public void testSelectByOneToManyAttribute() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .fetch(Fetch.innerFetch("posts"))
        .where(like(path("posts", "title"), "%-4"));
    List<UserDb> users = getResultList(criteriaQuery);
    assertEquals(10, users.size());
    users.forEach(user -> assertEquals(1, user.getPosts().size()));
  }

  @Test
  public void testSelectByOneToManyAttributeWithoutFetching() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .join(Join.innerJoin("posts"))
        .where(like(path("posts", "title"), "%-4"));
    List<UserDb> users = getResultList(criteriaQuery);
    assertEquals(10, users.size());

    users.forEach(user -> assertThrows(LazyInitializationException.class, () -> user.getPosts().size()));
  }

  @Test
  public void testSelectTupleByOneToManyAttribute() {
    DetachedCriteriaQuery<Tuple> criteriaQuery = multiselect(path("posts", "comments", "text"))
        .intoTuple()
        .from(UserDb.class)
        .join(Join.innerJoin("posts").addChild(Join.innerJoin("comments")))
        .where(like(path("posts", "comments", "text"), "comment-1-1-%"));
    List<Tuple> rows = getResultList(criteriaQuery);
    assertEquals(10, rows.size());
  }

  @Test
  public void testSelectByManyToOneAttribute() {
    DetachedCriteriaQuery<CommentDb> criteriaQuery = selectEntity(CommentDb.class)
        .fetch(Fetch.innerFetch("post").addChild(Fetch.innerFetch("user")))
        .where(
            and(
                equal(path("post", "user", "name"), "name-1"),
                equal(path("post", "title"), "post-1-4")
            )
        );
    List<CommentDb> comments = getResultList(criteriaQuery);
    assertEquals(10, comments.size());
  }

  private static class UserBriefInfo {
    private final int id;
    private final String login;
    private final String email;

    public UserBriefInfo(int id, String login, String email) {
      this.id = id;
      this.login = login;
      this.email = email;
    }

    public int getId() {
      return id;
    }

    public String getLogin() {
      return login;
    }

    public String getEmail() {
      return email;
    }
  }
}
