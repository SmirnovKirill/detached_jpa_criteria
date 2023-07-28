package kirill.detachedjpacriteria.query;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.metamodel.IdentifiableType;
import kirill.detachedjpacriteria.AbstractTest;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.id;
import static kirill.detachedjpacriteria.expression.api.DetachedCriteriaBuilder.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class MultiIdTest extends AbstractTest {
  @Test
  public void testSelectMultiIdEmbedded() {
    EntityWithCompositeIdEmbedded first = new EntityWithCompositeIdEmbedded(new EntityIdEmbedded("first left", "first right"), "column first");
    EntityWithCompositeIdEmbedded second = new EntityWithCompositeIdEmbedded(new EntityIdEmbedded("second left", "second right"), "column second");
    EntityWithCompositeIdEmbedded third = new EntityWithCompositeIdEmbedded(new EntityIdEmbedded("third left", "third right"), "column third");
    
    doInTransaction(entityManager -> {
      entityManager.persist(first);
      entityManager.persist(second);
      entityManager.persist(third);
    });

    List<EntityIdEmbedded> entityIds = readInTransaction(entityManager ->
        select(id()).into(EntityIdEmbedded.class).from(EntityWithCompositeIdEmbedded.class)
            .orderByAsc(id())
            .createJpaQuery(entityManager).getResultList()
    );
    assertEquals(3, entityIds.size());
    assertEquals("first left", entityIds.get(0).getFirstPart());
    assertEquals("second left", entityIds.get(1).getFirstPart());
    assertEquals("third left", entityIds.get(2).getFirstPart());
  }


  /*
   * Несмотря на то что Хибер поддерживает возможность создавать композитные первичные ключи без отдельного класса, в JPA такое делать нельзя, об
   * этом сказано в документации Хибера https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#identifiers-composite
   *
   * The restriction that a composite identifier has to be represented by a "primary key class" (e.g. @EmbeddedId or @IdClass) is only Jakarta
   * Persistence-specific.
   *
   * Hibernate does allow composite identifiers to be defined without a "primary key class" via multiple @Id attributes, although that is generally
   * considered poor design.
   */
  @Test
  public void testSelectMultiIdMulti() {
    EntityWithCompositeIdMulti first = new EntityWithCompositeIdMulti("first left", "first right", "column first");
    EntityWithCompositeIdMulti second = new EntityWithCompositeIdMulti("second left", "second right", "column second");
    EntityWithCompositeIdMulti third = new EntityWithCompositeIdMulti("third left", "third right", "column third");

    doInTransaction(entityManager -> {
      entityManager.persist(first);
      entityManager.persist(second);
      entityManager.persist(third);
    });

    doInTransaction(entityManager -> {
      IdentifiableType<EntityWithCompositeIdMulti> it = (IdentifiableType<EntityWithCompositeIdMulti>) entityManager.getMetamodel()
          .managedType(EntityWithCompositeIdMulti.class);
      assertNull(it.getIdType());
    });

    assertThrows(NullPointerException.class, () -> readInTransaction(entityManager ->
        select(id()).into(Object.class).from(EntityWithCompositeIdMulti.class)
            .orderByAsc(id())
            .createJpaQuery(entityManager).getResultList()
    ));
  }

  @Entity
  @Table(name = "entity_with_composite_id_embedded")
  public static class EntityWithCompositeIdEmbedded {
    @Id
    private EntityIdEmbedded id;

    @Column(name = "data", nullable = false)
    private String data;

    protected EntityWithCompositeIdEmbedded() {
    }

    public EntityWithCompositeIdEmbedded(EntityIdEmbedded id, String data) {
      this.id = id;
      this.data = data;
    }

    public EntityIdEmbedded getId() {
      return id;
    }

    public void setId(EntityIdEmbedded id) {
      this.id = id;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }
  }

  @Embeddable
  public static class EntityIdEmbedded implements Serializable {
    @Column(name = "first_part", nullable = false)
    private String firstPart;
    @Column(name = "second_part", nullable = false)
    private String secondPart;

    protected EntityIdEmbedded() {
    }

    public EntityIdEmbedded(String firstPart, String secondPart) {
      this.firstPart = firstPart;
      this.secondPart = secondPart;
    }

    public String getFirstPart() {
      return firstPart;
    }

    public String getSecondPart() {
      return secondPart;
    }
  }

  @Entity
  @Table(name = "entity_with_composite_id_multi")
  public static class EntityWithCompositeIdMulti implements Serializable {
    @Id
    @Column(name = "first_part", nullable = false)
    private String firstPart;

    @Id
    @Column(name = "second_part", nullable = false)
    private String secondPart;

    @Column(name = "data", nullable = false)
    private String data;

    protected EntityWithCompositeIdMulti() {
    }

    public EntityWithCompositeIdMulti(String firstPart, String secondPart, String data) {
      this.firstPart = firstPart;
      this.secondPart = secondPart;
      this.data = data;
    }

    public String getFirstPart() {
      return firstPart;
    }

    public void setFirstPart(String firstPart) {
      this.firstPart = firstPart;
    }

    public String getSecondPart() {
      return secondPart;
    }

    public void setSecondPart(String secondPart) {
      this.secondPart = secondPart;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }
  }
}
