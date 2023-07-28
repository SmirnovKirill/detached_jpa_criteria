package kirill.detachedjpacriteria.expression.impl;

import java.util.Map;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

public class PathContext {
  private final Root<?> root;
  private final Map<AttributePath, Path<?>> fetchPaths; // Зачем это нужно можно прочитать в DetachedExpressionType::getPath.
  private final Map<AttributePath, Path<?>> joinPaths; // Зачем это нужно можно прочитать в DetachedExpressionType::getPath.

  public PathContext(Root<?> root, Map<AttributePath, Path<?>> fetchPaths, Map<AttributePath, Path<?>> joinPaths) {
    this.root = root;
    this.fetchPaths = fetchPaths;
    this.joinPaths = joinPaths;
  }

  public Root<?> getRoot() {
    return root;
  }

  public Map<AttributePath, Path<?>> getFetchPaths() {
    return fetchPaths;
  }

  public Map<AttributePath, Path<?>> getJoinPaths() {
    return joinPaths;
  }
}
