package kirill.detachedjpacriteria.query;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.persistence.criteria.JoinType;
import kirill.detachedjpacriteria.util.Util;

/**
 * Класс иммутабельный и поэтому thread-safe.
 */
public final class Fetch {
  private final String attributeName;
  private final JoinType joinType;
  private final List<Fetch> children;

  private Fetch(String attributeName, JoinType joinType, List<Fetch> children) {
    this.attributeName = attributeName;
    this.joinType = joinType;
    this.children = children;
  }

  private Fetch(String attributeName, JoinType joinType) {
    this(attributeName, joinType, List.of());
  }

  public static Fetch fetch(String attributeName, JoinType joinType) {
    return new Fetch(attributeName, joinType);
  }

  public static Fetch innerFetch(String attributeName) {
    return fetch(attributeName, JoinType.INNER);
  }

  public static Fetch leftFetch(String attributeName) {
    return fetch(attributeName, JoinType.LEFT);
  }

  public static List<Fetch> leftFetches(String... attributeNames) {
    return Stream.of(attributeNames).map(Fetch::leftFetch).collect(toList());
  }

  public static List<Fetch> innerFetches(String... attributeNames) {
    return Stream.of(attributeNames).map(Fetch::innerFetch).collect(toList());
  }

  public Fetch addChild(Fetch child) {
    return new Fetch(attributeName, joinType, Util.unmodifiableUnion(children, child));
  }

  public Join toJoinDeep() {
    List<Join> convertedChildren = new ArrayList<>();
    for (Fetch child : children) {
      convertedChildren.add(child.toJoinDeep());
    }

    return new Join(attributeName, joinType, convertedChildren);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public List<Fetch> getChildren() {
    return children;
  }
}
