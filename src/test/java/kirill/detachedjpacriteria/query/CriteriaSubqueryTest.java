package kirill.detachedjpacriteria.query;

import java.util.List;
import kirill.detachedjpacriteria.AbstractTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kirill.detachedjpacriteria.entity.UserDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.exists;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.id;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.like;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.literal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parameter;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parentId;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parentPath;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parentRoot;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.root;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.subquerySelect;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.subquerySelectEntity;

public class CriteriaSubqueryTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testSimpleSelect() {
    List<UserDb> users = getResultList(
        selectEntity(UserDb.class)
            .where(
                id().in(
                    subquerySelect(id())
                        .into(Integer.class)
                        .from(UserDb.class)
                        .where(equal(path("email"), "user-5@mail.com"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());
  }

  @Test
  public void testSimpleSelectWithParameter() {
    List<UserDb> users = getResultList(
        selectEntity(UserDb.class)
            .where(
                id().in(
                    subquerySelect(id())
                        .into(Integer.class)
                        .from(UserDb.class)
                        .where(equal(path("email"), parameter("email")))
                )
            )
            .parameter("email", "user-5@mail.com")
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());
  }

  @Test
  public void testSelectEntity() {
    List<UserDb> users = getResultList(
        selectEntity(UserDb.class)
            .where(
                root().in(
                    subquerySelectEntity(UserDb.class).where(equal(path("email"), "user-5@mail.com"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());
  }

  @Test
  public void testParentPaths() {
    List<UserDb> users = getResultList(
        selectEntity(UserDb.class)
            .where(
                exists(
                    subquerySelect(literal(1)).into(Integer.class).from(UserDb.class)
                        .where(equal(path("email"), parentPath("email")))
                        .where(equal(path("email"), "user-5@mail.com"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());

    users = getResultList(
        selectEntity(UserDb.class)
            .where(
                exists(
                    subquerySelect(literal(1)).into(Integer.class).from(UserDb.class)
                        .where(equal(id(), parentId()))
                        .where(equal(path("email"), "user-5@mail.com"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());

    users = getResultList(
        selectEntity(UserDb.class)
            .where(
                exists(
                    subquerySelect(literal(1)).into(Integer.class).from(UserDb.class)
                        .where(equal(root(), parentRoot()))
                        .where(equal(path("email"), "user-5@mail.com"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-5@mail.com", users.get(0).getEmail());
  }

  @Test
  public void testJoins() {
    List<UserDb> users = getResultList(
        selectEntity(UserDb.class)
            .where(
                root().in(
                    subquerySelectEntity(UserDb.class)
                        .innerJoin("posts")
                        .where(like(path("posts", "title"), "post-6-%"))
                )
            )
    );
    assertEquals(1, users.size());
    assertEquals("user-6@mail.com", users.get(0).getEmail());
  }
}
