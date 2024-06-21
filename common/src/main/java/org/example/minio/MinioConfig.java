package org.example.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {
  /**
   * Get MinioClient.
   *
   * @param properties application props
   * @return MinioClient
   */
  @Bean
  public MinioClient minioClient(MinioProperties properties) throws Exception {
    var client = MinioClient.builder()
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .endpoint(properties.getUrl(), properties.getPort(), properties.isSecure())
            .build();

    var tags = new HashMap<String, String>();
    tags.put("status", "wipImage");
    RuleFilter filter = new RuleFilter(new AndOperator("", tags));

    var lifecycleRules = new ArrayList<LifecycleRule>();
    ZonedDateTime zonedDateTime = null;
    lifecycleRules.add(new LifecycleRule(
            Status.ENABLED,
            null,
            new Expiration(zonedDateTime, properties.getTtlInDays(), null),
            filter,
            "imageExpirationRule",
            null,
            null,
            null));
    var lifecycleConfig = new LifecycleConfiguration(lifecycleRules);

    var bucketExists = client.bucketExists(
            BucketExistsArgs.builder().bucket(properties.getBucket()).build());
    if (!bucketExists) {
      client.makeBucket(MakeBucketArgs
              .builder()
              .bucket(properties.getBucket())
              .build());
    }
    client.setBucketLifecycle(SetBucketLifecycleArgs
            .builder()
            .bucket(properties.getBucket())
            .config(lifecycleConfig)
            .build());
    return client;
  }
}
