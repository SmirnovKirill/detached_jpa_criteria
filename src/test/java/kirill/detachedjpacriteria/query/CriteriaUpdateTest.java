package kirill.detachedjpacriteria.query;

import java.util.List;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder;
import kirill.detachedjpacriteria.expression.api.DetachedPath;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaUpdate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kirill.detachedjpacriteria.entity.CommentDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.like;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.or;

public class CriteriaUpdateTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testUpdateWithoutParameter() {
    DetachedCriteriaUpdate<CommentDb> criteriaUpdate = DetachedCriteriaBuilder.update(CommentDb.class)
        .set("text", "new value")
        .where(DetachedCriteriaBuilder.or(DetachedCriteriaBuilder.like(DetachedCriteriaBuilder.path("text"), "comment-1-%"), DetachedCriteriaBuilder.like(
            DetachedCriteriaBuilder.path("text"), "comment-2-%")));

    int updatedEntities = runUpdate(criteriaUpdate);
    assertEquals(100, updatedEntities);

    List<CommentDb> comments = getResultList(DetachedCriteriaBuilder.selectEntity(CommentDb.class).where(DetachedCriteriaBuilder.equal(
        DetachedCriteriaBuilder.path("text"), "new value")));

    assertEquals(100, comments.size());
  }

  @Test
  public void testUpdateWithParameter() {
    DetachedPath<String> textPath = DetachedCriteriaBuilder.path("text"); //Иначе будет ambiguous call, в JPA ровно так же.
    DetachedCriteriaUpdate<CommentDb> criteriaUpdate = DetachedCriteriaBuilder.update(CommentDb.class)
        .set(textPath, DetachedCriteriaBuilder.parameter("value"))
        .where(DetachedCriteriaBuilder.or(DetachedCriteriaBuilder.like(DetachedCriteriaBuilder.path("text"), "comment-1-%"), DetachedCriteriaBuilder.like(
            DetachedCriteriaBuilder.path("text"), "comment-2-%")))
        .parameter("value", "new value");

    int updatedEntities = runUpdate(criteriaUpdate);
    assertEquals(100, updatedEntities);

    List<CommentDb> comments = getResultList(DetachedCriteriaBuilder.selectEntity(CommentDb.class).where(DetachedCriteriaBuilder.equal(
        DetachedCriteriaBuilder.path("text"), "new value")));

    assertEquals(100, comments.size());
  }
}
