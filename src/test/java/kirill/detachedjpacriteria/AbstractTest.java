package kirill.detachedjpacriteria;

import static java.lang.String.format;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import kirill.detachedjpacriteria.entity.CommentDb;
import kirill.detachedjpacriteria.entity.PostDb;
import kirill.detachedjpacriteria.entity.UserDb;
import kirill.detachedjpacriteria.entity.UserType;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.delete;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.root;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaDelete;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaUpdate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public abstract class AbstractTest {
  protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

  @Inject
  private MappingConfig mappingConfig;

  @Inject
  protected TransactionalScope txScope;

  @Inject
  protected SessionFactory sessionFactory;

  @BeforeEach
  public void truncateTables() {
    Arrays.stream(mappingConfig.getAnnotatedClasses())
        .forEach(entityClass -> runDelete(delete(entityClass)));
  }

  protected void insertTestData() {
    createAndPersistUsers();
  }

  private void createAndPersistUsers() {
    int usersToInsertCount = 10;
    for (int i = 1; i <= usersToInsertCount; i++) {
      UserDb user = new UserDb(
          format("user-%d", i),
          format("user-%d@mail.com", i),
          i % 2 == 0 ? UserType.ADMINISTRATOR : UserType.USER,
          i % 2 == 0 ? UserType.ADMINISTRATOR : UserType.USER,
          i % 2 == 0,
          format("name-%d", i),
          format("last-name-%d", i),
          ZonedDateTime.parse(String.format("%02d.01.2020 %02d:00:00", i + 1, i + 1), FORMATTER),
          null
      );
      sessionFactory.getCurrentSession().persist(user);

      List<PostDb> posts = createPosts(user, i);
      posts.forEach(user::addPost);
    }
  }

  private List<PostDb> createPosts(UserDb user, int userIndex) {
    List<PostDb> posts = new ArrayList<>();

    int postsToInsertCount = 5;
    for (int i = 1; i <= postsToInsertCount; i++) {
      PostDb post = new PostDb(
          user,
          format("post-%d-%d", userIndex, i),
          ZonedDateTime.now()
      );
      sessionFactory.getCurrentSession().persist(post);

      List<CommentDb> comments = createComments(post, userIndex, i);
      comments.forEach(post::addComment);

      posts.add(post);
    }

    return posts;
  }

  private List<CommentDb> createComments(PostDb post, int userIndex, int postIndex) {
    List<CommentDb> comments = new ArrayList<>();

    int commentsToInsertCount = 10;
    for (int i = 1; i <= commentsToInsertCount; i++) {
      CommentDb comment = new CommentDb(
          post,
          format("comment-%d-%d-%d", userIndex, postIndex, i),
          ZonedDateTime.now()
      );
      sessionFactory.getCurrentSession().persist(comment);

      comments.add(comment);
    }

    return comments;
  }

  protected <T> T readInTransaction(Function<EntityManager, T> readFunction) {
    return txScope.doInTransaction(() -> {
      EntityManager entityManager = sessionFactory.getCurrentSession();
      return readFunction.apply(entityManager);
    });
  }

  protected void doInTransaction(Consumer<EntityManager> transactionFunction) {
    txScope.doInTransaction(() -> {
      EntityManager entityManager = sessionFactory.getCurrentSession();
      transactionFunction.accept(entityManager);
    });
  }

  protected <T> List<T> getResultList(DetachedCriteriaQuery<T> criteriaQuery) {
    return readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
  }

  protected <T> T getSingleResult(DetachedCriteriaQuery<T> criteriaQuery) {
    return readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getSingleResult());
  }

  protected long getCount(DetachedCriteriaQuery<?> criteriaQuery) {
    return readInTransaction(entityManager -> criteriaQuery.toCountCriteriaQuery(root()).createJpaQuery(entityManager).getSingleResult());
  }

  protected <T> List<T> getResultListWithBatches(DetachedCriteriaQuery<T> criteriaQuery) {
    return readInTransaction(entityManager -> {
      List<T> result = new ArrayList<>();
      for (TypedQuery<T> query : criteriaQuery.createJpaBatchQueries(entityManager, 2)) {
        result.addAll(query.getResultList());
      }

      return result;
    });
  }

  protected int runDelete(DetachedCriteriaDelete<?> criteriaDelete) {
    return txScope.doInTransaction(() -> {
      EntityManager entityManager = sessionFactory.getCurrentSession();
      return criteriaDelete.createJpaQuery(entityManager).executeUpdate();
    });
  }

  protected int runUpdate(DetachedCriteriaUpdate<?> criteriaUpdate) {
    return txScope.doInTransaction(() -> {
      EntityManager entityManager = sessionFactory.getCurrentSession();
      return criteriaUpdate.createJpaQuery(entityManager).executeUpdate();
    });
  }
}
