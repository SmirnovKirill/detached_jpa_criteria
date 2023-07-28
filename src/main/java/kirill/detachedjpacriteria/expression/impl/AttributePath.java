package kirill.detachedjpacriteria.expression.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static kirill.detachedjpacriteria.util.Util.unmodifiableUnion;

/**
 * Класс иммутабельный и поэтому thread-safe.
 */
public class AttributePath {
  private final List<String> attributeNames;

  private AttributePath(List<String> attributeNames) {
    this.attributeNames = List.copyOf(attributeNames.stream().filter(Objects::nonNull).collect(Collectors.toList()));
  }

  public static AttributePath path(String... attributeNames) {
    return new AttributePath(Arrays.asList(attributeNames));
  }

  public static AttributePath emptyPath() {
    return path();
  }

  public AttributePath with(String attributeName) {
    return new AttributePath(unmodifiableUnion(attributeNames, attributeName));
  }

  /**
   * Возвращает копию объекта с удаленным последним атрибутом.
   * @throws IllegalStateException если объект уже пустой и нечего удалять.
   */
  public AttributePath withoutLastAttribute() {
    if (isEmpty()) {
      throw new IllegalStateException();
    }

    return new AttributePath(new ArrayList<>(attributeNames).subList(0, attributeNames.size() - 1));
  }

  public boolean isEmpty() {
    return attributeNames.isEmpty();
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AttributePath attributePath = (AttributePath) o;
    return Objects.equals(attributeNames, attributePath.attributeNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeNames);
  }
}
