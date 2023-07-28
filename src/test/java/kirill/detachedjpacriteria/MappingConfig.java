package kirill.detachedjpacriteria;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MappingConfig {
  private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

  public MappingConfig(Class<?>... entityClasses) {
    annotatedClasses.addAll(Arrays.asList(entityClasses));
  }

  public Class<?>[] getAnnotatedClasses() {
    return annotatedClasses.toArray(Class<?>[]::new);
  }
}
