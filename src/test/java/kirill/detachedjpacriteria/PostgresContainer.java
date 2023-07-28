package kirill.detachedjpacriteria;

import java.util.List;
import java.util.Optional;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer {
  public static final PostgreSQLContainer<?> INSTANCE = createPostgresContainer();
  public static final String DEFAULT_USER = "postgres";
  public static final String DEFAULT_PASSWORD = "test";

  private static final String IMAGE_NAME_ENV_VARIABLE = "EXT_POSTGRES_IMAGE";
  private static final String DEFAULT_IMAGE_NAME = "postgres:13.5";

  private static PostgreSQLContainer<?> createPostgresContainer() {
    String imageName = Optional.ofNullable(System.getenv(IMAGE_NAME_ENV_VARIABLE)).orElse(DEFAULT_IMAGE_NAME);

    PostgreSQLContainer<?> container = new PostgreSQLContainer<>(imageName)
        .withUsername(DEFAULT_USER)
        .withPassword(DEFAULT_PASSWORD);
    container.setEnv(List.of("LC_ALL=en_US.UTF-8", "LC_COLLATE=ru_RU.UTF-8", "LC_CTYPE=ru_RU.UTF-8"));
    container.setCommand(
        "postgres",
        "-c", "fsync=off",
        "-c", "autovacuum=off"
    );
    container.start();

    return container;
  }
}
