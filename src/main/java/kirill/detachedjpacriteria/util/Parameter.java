package kirill.detachedjpacriteria.util;

public class Parameter<T> {
  private final String name;
  private final Class<T> valueClass;
  private final T value;

  public Parameter(String name, Class<T> valueClass, T value) {
    this.name = name;
    this.valueClass = valueClass;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Class<T> getValueClass() {
    return valueClass;
  }

  public T getValue() {
    return value;
  }
}
