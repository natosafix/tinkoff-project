package org.example.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

import static java.util.Objects.isNull;

public class PostgreTestConfig {
  private static volatile PostgreSQLContainer<?> postgreSQLContainer = null;

  private static PostgreSQLContainer getPostgreSQLContainer() {
    PostgreSQLContainer instance = postgreSQLContainer;
    if (isNull(instance)) {
      synchronized (PostgreSQLContainer.class) {
        instance = postgreSQLContainer;
        if (isNull(instance)) {
          postgreSQLContainer = instance = new PostgreSQLContainer<>("postgres:14.7")
                  .withDatabaseName("testDB")
                  .withUsername("postgres")
                  .withPassword("postgres")
                  .withStartupTimeout(Duration.ofSeconds(60))
                  .withReuse(true);
          postgreSQLContainer.start();
        }
      }
    }
    return instance;
  }

  @Component("ProstgresInitializer")
  public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      var postgreSQLContainer = getPostgreSQLContainer();

      var jdbcUrl = postgreSQLContainer.getJdbcUrl();
      var username = postgreSQLContainer.getUsername();
      var password = postgreSQLContainer.getPassword();

      TestPropertyValues.of(
              "spring.datasource.url=" + jdbcUrl,
              "spring.datasource.username=" + username,
              "spring.datasource.password" + password,
              "spring.datasource.driverClassName=org.hibernate.dialect.PostgreSQLDialect"
      ).applyTo(applicationContext.getEnvironment());
    }
  }
}
