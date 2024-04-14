package org.example.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .endpoint(properties.getUrl(), properties.getPort(), properties.isSecure())
            .build();
  }
}
