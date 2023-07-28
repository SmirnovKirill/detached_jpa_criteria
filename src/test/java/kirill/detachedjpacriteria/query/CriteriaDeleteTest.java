package kirill.detachedjpacriteria.query;

import java.util.List;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.entity.CommentDb;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.delete;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.like;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.or;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaDelete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CriteriaDeleteTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testDelete() {
    DetachedCriteriaDelete<CommentDb> criteriaDelete = delete(CommentDb.class)
        .where(or(equal(path("text"), "comment-1-1-5"), like(path("text"), "comment-2-%")));

    int removedEntities = runDelete(criteriaDelete);
    assertEquals(51, removedEntities);

    List<CommentDb> comments = getResultList(selectEntity(CommentDb.class));

    assertEquals(449, comments.size());
  }
}
