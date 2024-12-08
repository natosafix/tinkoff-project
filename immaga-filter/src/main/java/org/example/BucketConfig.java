package org.example;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL;
import io.github.bucket4j.postgresql.PostgreSQLSelectForUpdateBasedProxyManager;
import io.github.bucket4j.postgresql.PostgreSQLadvisoryLockBasedProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static java.time.Duration.ofSeconds;

@Configuration
public class BucketConfig {

  @Bean
  public DataSource dataSource(
          @Value("${spring.datasource.url}") String dataSourceUrl,
          @Value("${spring.datasource.username}") String dataSourceUsername,
          @Value("${spring.datasource.password}") String dataSourcePassword
  ) {
    return DataSourceBuilder.create()
            .url(dataSourceUrl)
            .username(dataSourceUsername)
            .password(dataSourcePassword)
            .build();
  }

  @Bean
  public Bucket bucket(DataSource dataSource) {
    PostgreSQLSelectForUpdateBasedProxyManager<Long> proxyManager = Bucket4jPostgreSQL
            .selectForUpdateBasedBuilder(dataSource)
            .build();

    Long key = 1L;
    BucketConfiguration bucketConfiguration = BucketConfiguration.builder()
            .addLimit(limit -> limit.capacity(10).refillGreedy(10, ofSeconds(1)))
            .build();
    return proxyManager.getProxy(key, () -> bucketConfiguration);
  }
}
