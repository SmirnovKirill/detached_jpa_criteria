package kirill.detachedjpacriteria.query;

import java.time.ZonedDateTime;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import kirill.detachedjpacriteria.AbstractTest;
import kirill.detachedjpacriteria.entity.UserDb;
import kirill.detachedjpacriteria.entity.UserType;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.equal;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.greaterThan;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.lessThan;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.parameter;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.path;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.selectEntity;
import kirill.detachedjpacriteria.query.api.DetachedCriteriaQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParametersTest extends AbstractTest {
  @BeforeEach
  public void setup() {
    txScope.doInTransaction(this::insertTestData);
  }

  @Test
  public void testEnumStringParameter() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeStr"), parameter("userType")))
        .parameter("userType", UserType.ADMINISTRATOR);
    List<UserDb> users = getResultList(detachedQuery);
    assertEquals(5, users.size());
    assertTrue(users.stream().allMatch(user -> user.getUserTypeStr() == UserType.ADMINISTRATOR));
  }

  //Когда енум то его надо так и передавать енумом, строкой нельзя (в JPA тоже, только там другой эксепшен потому что тип явно задается).
  @Test
  public void testEnumStringParameterFail() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<UserDb> criteriaQuery = criteriaBuilder.createQuery(UserDb.class);
          Root<UserDb> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.select(root);
          criteriaQuery.where(criteriaBuilder.equal(root.get("userTypeStr"), criteriaBuilder.parameter(UserType.class, "userType")));
          entityManager.createQuery(criteriaQuery)
              .setParameter("userType", UserType.ADMINISTRATOR.toString())
              .getSingleResult();
        })
    );
    assertEquals("Named parameter [userType] type mismatch; expecting [UserType] but found [String]", exception.getMessage());

    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeStr"), parameter("userType")))
        .parameter("userType", UserType.ADMINISTRATOR.toString());
    exception = assertThrows(ClassCastException.class, () -> getResultList(detachedQuery));
    assertTrue(exception.getMessage().startsWith("class java.lang.String cannot be cast to class java.lang.Enum"));
  }

  @Test
  public void testEnumStringInline() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeStr"), UserType.ADMINISTRATOR));
    List<UserDb> users = getResultList(detachedQuery);
    assertEquals(5, users.size());
    assertTrue(users.stream().allMatch(user -> user.getUserTypeStr() == UserType.ADMINISTRATOR));
  }

  @Test
  public void testEnumOrdinalParameter() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeOrd"), parameter("userType")))
        .parameter("userType", UserType.ADMINISTRATOR);
    List<UserDb> users = getResultList(detachedQuery);
    assertEquals(5, users.size());
    assertTrue(users.stream().allMatch(user -> user.getUserTypeOrd() == UserType.ADMINISTRATOR));
  }

  //Когда енум то его надо так и передавать енумом, строкой нельзя (в JPA тоже, только там другой эксепшен потому что тип явно задается).
  @Test
  public void testEnumOrdinalParameterFail() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> doInTransaction(entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<UserDb> criteriaQuery = criteriaBuilder.createQuery(UserDb.class);
          Root<UserDb> root = criteriaQuery.from(UserDb.class);
          criteriaQuery.select(root);
          criteriaQuery.where(criteriaBuilder.equal(root.get("userTypeOrd"), criteriaBuilder.parameter(UserType.class, "userType")));
          entityManager.createQuery(criteriaQuery)
              .setParameter("userType", UserType.ADMINISTRATOR.toString())
              .getSingleResult();
        })
    );
    assertEquals("Named parameter [userType] type mismatch; expecting [UserType] but found [String]", exception.getMessage());

    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeOrd"), parameter("userType")))
        .parameter("userType", UserType.ADMINISTRATOR.toString());
    exception = assertThrows(ClassCastException.class, () -> getResultList(detachedQuery));
    assertTrue(exception.getMessage().startsWith("class java.lang.String cannot be cast to class java.lang.Enum"));
  }

  @Test
  public void testEnumOrdinalInline() {
    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeOrd"), UserType.ADMINISTRATOR));
    List<UserDb> users = getResultList(detachedQuery);
    assertEquals(5, users.size());
    assertTrue(users.stream().allMatch(user -> user.getUserTypeOrd() == UserType.ADMINISTRATOR));

    detachedQuery = selectEntity(UserDb.class)
        .where(equal(path("userTypeOrd"), UserType.ADMINISTRATOR.ordinal()));
    users = getResultList(detachedQuery);
    assertEquals(5, users.size());
    assertTrue(users.stream().allMatch(user -> user.getUserTypeOrd() == UserType.ADMINISTRATOR));
  }

  @Test
  public void testDateParameter() {
    ZonedDateTime thresholdDate = ZonedDateTime.parse("05.01.2020 00:00:00", FORMATTER);

    DetachedCriteriaQuery<UserDb> detachedQuery = selectEntity(UserDb.class)
        .where(lessThan(path("created"), parameter("created")))
        .parameter("created", thresholdDate);
    List<UserDb> users = getResultList(detachedQuery);
    assertEquals(3, users.size());
    assertTrue(users.stream().allMatch(user -> user.getCreated().isBefore(thresholdDate)));

    detachedQuery = selectEntity(UserDb.class)
        .where(greaterThan(path("created"), parameter("created")))
        .parameter("created", thresholdDate);
    users = getResultList(detachedQuery);
    assertEquals(7, users.size());
    assertTrue(users.stream().allMatch(user -> user.getCreated().isAfter(thresholdDate)));
  }
}
