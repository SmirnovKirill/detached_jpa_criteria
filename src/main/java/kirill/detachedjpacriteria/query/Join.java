package kirill.detachedjpacriteria.query;

import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.persistence.criteria.JoinType;
import kirill.detachedjpacriteria.util.Util;

/**
 * Класс иммутабельный и поэтому thread-safe.
 */
public final class Join {
  private final String attributeName;
  private final JoinType joinType;
  private final List<Join> children;

  Join(String attributeName, JoinType joinType, List<Join> children) {
    this.attributeName = attributeName;
    this.joinType = joinType;
    this.children = children;
  }

  private Join(String attributeName, JoinType joinType) {
    this(attributeName, joinType, List.of());
  }

  public static Join join(String attributeName, JoinType joinType) {
    return new Join(attributeName, joinType);
  }

  public static Join innerJoin(String attributeName) {
    return join(attributeName, JoinType.INNER);
  }

  public static Join leftJoin(String attributeName) {
    return join(attributeName, JoinType.LEFT);
  }

  public static List<Join> leftJoins(String... attributeNames) {
    return Stream.of(attributeNames).map(Join::leftJoin).collect(toList());
  }

  public static List<Join> innerJoins(String... attributeNames) {
    return Stream.of(attributeNames).map(Join::innerJoin).collect(toList());
  }

  public Join addChild(Join child) {
    return new Join(attributeName, joinType, Util.unmodifiableUnion(children, child));
  }

  public String getAttributeName() {
    return attributeName;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public List<Join> getChildren() {
    return children;
  }
}
