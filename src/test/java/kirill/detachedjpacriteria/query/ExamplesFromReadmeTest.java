package kirill.detachedjpacriteria.query;

import java.util.List;
import javax.persistence.Tuple;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.entity.CommentDb;
import kirill.detachedjpacriteria.entity.PostDb;
import kirill.detachedjpacriteria.entity.UserDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.delete;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.id;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.like;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.literal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.multiselect;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parameter;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.root;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.select;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectCount;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.subquerySelect;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.update;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaDelete;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaUpdate;
import kirill.detachedjpacriteria.query.api.QueryCopyPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExamplesFromReadmeTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testSelectWithFilters() {
    boolean someBusinessCondition = true;

    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .innerFetch("posts")
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()
    if (someBusinessCondition) {
      criteriaQuery.where(like(path("posts", "title"), parameter("title"))); //Вызов добавляет условие к уже имеющемуся
      criteriaQuery.parameter("title", "post-1%");
    }
    criteriaQuery.orderByDesc(path("login"));
    criteriaQuery.distinct(true);

    List<UserDb> users = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
    assertEquals(2, users.size());
  }

  @Test
  public void testUpdate() {
    DetachedCriteriaUpdate<UserDb> criteriaQuery = update(UserDb.class) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .set("login", "updated login")
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    doInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).executeUpdate());

    List<UserDb> allUsers = getResultList(selectEntity(UserDb.class).orderByAsc(id()));
    assertEquals(10, allUsers.size());
    for (int i = 0; i < 10; i++) {
      if (i == 0 || i == 9) {
        assertEquals("updated login", allUsers.get(i).getLogin());
      } else {
        assertNotEquals("updated login", allUsers.get(i).getLogin());
      }
    }
  }

  @Test
  public void testDelete() {
    DetachedCriteriaDelete<CommentDb> criteriaQuery = delete(CommentDb.class) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .where(like(path("text"), parameter("text"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("text", "comment-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    doInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).executeUpdate());

    List<CommentDb> allComments = getResultList(selectEntity(CommentDb.class).orderByAsc(id()));
    assertEquals(400, allComments.size());
  }

  @Test
  public void testMultiselect() {
    DetachedCriteriaQuery<IdWithLoginAndPost> criteriaQuery = multiselect(id(), path("login"), path("posts", "title")) //Что выбираем
        .into(IdWithLoginAndPost.class) //Объекты какого класса будут возвращаться
        .from(UserDb.class) //Для какой сущности
        .leftJoin("posts")
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    List<IdWithLoginAndPost> idsWithLoginsAndPosts = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
    assertEquals(10, idsWithLoginsAndPosts.size());
  }

  @Test
  public void testSelectOneColumn() {
    DetachedCriteriaQuery<String> criteriaQuery = select(path("login")) //Что выбираем
        .into(String.class) //Объекты какого класса будут возвращаться
        .from(UserDb.class) //Для какой сущности
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    List<String> logins = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
    assertEquals(2, logins.size());
  }

  @Test
  public void testSelectTuple() {
    DetachedCriteriaQuery<Tuple> criteriaQuery = multiselect(id(), path("login"), path("posts", "title")) //Что выбираем
        .intoTuple() //Указываем что будет возвращаться Tuple
        .from(UserDb.class) //Для какой сущности
        .leftJoin("posts")
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    List<Tuple> idsWithLoginsAndPosts = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
    assertEquals(10, idsWithLoginsAndPosts.size());
  }

  @Test
  public void testSelectCount() {
    DetachedCriteriaQuery<Long> criteriaQuery = selectCount(root(), UserDb.class)
        .leftJoin("posts")
        .where(like(path("login"), parameter("login"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    long count = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getSingleResult());
    assertEquals(10, count);
  }

  @Test
  public void testSubquery() {
    DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
        .where(
            root().in(
                subquerySelect(path("user", "id")).into(Integer.class).from(PostDb.class)
                    .where(like(path("title"), "post-6-%"))
            )
        );

    List<UserDb> users = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
    assertEquals(1, users.size());
    assertEquals("user-6@mail.com", users.get(0).getEmail());
  }

  @Test
  public void testCopy() {
    DetachedCriteriaQuery<CommentDb> criteriaQuery = selectEntity(CommentDb.class)
        .where(like(path("text"), parameter("text")))
        .parameter("text", "comment-1-1-%");

    DetachedCriteriaDelete<CommentDb> criteriaDelete = delete(CommentDb.class);
    criteriaDelete.copyFromOtherCriteria(criteriaQuery, QueryCopyPart.COPY_WHERE, QueryCopyPart.COPY_PARAMS); //Указываем откуда и что копировать.

    int rowsDeleted = readInTransaction(entityManager -> criteriaDelete.createJpaQuery(entityManager).executeUpdate());
    assertEquals(10, rowsDeleted);
  }

  @Test
  public void testToCountCriteriaQuery() {
    DetachedCriteriaDelete<CommentDb> criteriaDelete = delete(CommentDb.class) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .where(like(path("text"), parameter("text"))) //Импорт из kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder
        .parameter("text", "comment-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

    long rowsToDelete = readInTransaction((entityManager) ->
        criteriaDelete.toCountCriteriaQuery(literal(1)).createJpaQuery(entityManager).getSingleResult()
    );
    assertEquals(100, rowsToDelete);

    rowsToDelete = readInTransaction((entityManager) ->
        criteriaDelete.toCountDistinctCriteriaQuery(id()).createJpaQuery(entityManager).getSingleResult()
    );
    assertEquals(100, rowsToDelete);
  }

  private static class IdWithLoginAndPost {
    private final int id;
    private final String login;
    private final String postTitle;

    public IdWithLoginAndPost(int id, String login, String postTitle) {
      this.id = id;
      this.login = login;
      this.postTitle = postTitle;
    }

    public int getId() {
      return id;
    }

    public String getLogin() {
      return login;
    }

    public String getPostTitle() {
      return postTitle;
    }
  }
}
