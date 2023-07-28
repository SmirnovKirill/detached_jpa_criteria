package kirill.detachedjpacriteria;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import kirill.detachedjpacriteria.entity.CommentDb;
import kirill.detachedjpacriteria.entity.PostDb;
import kirill.detachedjpacriteria.entity.UserDb;
import kirill.detachedjpacriteria.query.MultiIdTest;

@Configuration
@EnableTransactionManagement
public class TestConfig {
  @Bean
  public DataSource dataSource() {
    HikariConfig hikariConfig = new HikariConfig();

    hikariConfig.setDriverClassName("org.postgresql.Driver");
    hikariConfig.setJdbcUrl(
        String.format("jdbc:postgresql://%s:%d/postgres", PostgresContainer.INSTANCE.getHost(), PostgresContainer.INSTANCE.getFirstMappedPort())
    );
    hikariConfig.setUsername(PostgresContainer.DEFAULT_USER);
    hikariConfig.setPassword(PostgresContainer.DEFAULT_PASSWORD);

    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public MappingConfig mappingConfig() {
    return new MappingConfig(
        CommentDb.class,
        PostDb.class,
        UserDb.class,
        MultiIdTest.EntityWithCompositeIdEmbedded.class,
        MultiIdTest.EntityWithCompositeIdMulti.class
    );
  }

  @Bean
  public PropertiesFactoryBean hibernateProperties() {
    PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
    propertiesFactoryBean.setLocation(new ClassPathResource("hibernate-test.properties"));
    return propertiesFactoryBean;
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory(DataSource dataSource, MappingConfig mappingConfig, Properties hibernateProperties) {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();

    sessionFactory.setDataSource(dataSource);
    sessionFactory.setAnnotatedClasses(mappingConfig.getAnnotatedClasses());
    sessionFactory.setHibernateProperties(hibernateProperties);

    return sessionFactory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
    return new HibernateTransactionManager(sessionFactory);
  }

  @Bean
  public TransactionalScope transactionalScope() {
    return new TransactionalScope();
  }
}
