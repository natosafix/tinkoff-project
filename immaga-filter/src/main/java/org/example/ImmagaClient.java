package org.example;

import io.github.bucket4j.Bucket;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.xml.ws.http.HTTPException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class ImmagaClient {

  private final RestClient client;
  private final CircuitBreaker circuitBreaker;
  private final Retry retry;
  private final Bucket bucket;

  public ImmagaClient(Bucket bucket, @Value("${application.immaga.credentials}") String immagaCredentials) {
    this.bucket = bucket;

    client = RestClient.builder()
            .baseUrl("https://api.imagga.com/v2")
            .defaultHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(immagaCredentials.getBytes(StandardCharsets.UTF_8)))
            .build();

    circuitBreaker = CircuitBreaker.ofDefaults("circuitBreaker");
    var retryConfig = RetryConfig.custom()
            .retryOnException(this::isRetryableException)
            .build();
    var retryRegistry = RetryRegistry.of(retryConfig);
    this.retry = retryRegistry.retry("retry");
  }

  public String uploadImage(byte[] image) {
    var canSendRequest = bucket.tryConsume(1);
    if (!canSendRequest) {
      throw new RuntimeException();
    }

    return Decorators.ofSupplier(() -> uploadImageInternal(image))
            .withCircuitBreaker(circuitBreaker)
            .withRetry(retry)
            .get();
  }

  private String uploadImageInternal(byte[] image) {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

    parts.add("image", image);

    var response = client.post()
            .uri("/uploads")
            .body(parts)
            .retrieve()
            .toEntity(String.class);
    if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
      throw new HTTPException(response.getStatusCode().value());
    }

    var jsonObject = new JSONObject(response.getBody());
    if (jsonObject.has("result")) {
      return jsonObject.getJSONObject("result").getString("upload_id");
    }

    return null;
  }

  public String[] getTags(String uploadId) {
    var canSendRequest = bucket.tryConsume(1);
    if (!canSendRequest) {
      throw new RuntimeException();
    }

    return Decorators.ofSupplier(() -> getTagsInternal(uploadId))
            .withCircuitBreaker(circuitBreaker)
            .withRetry(retry)
            .get();
  }

  private String[] getTagsInternal(String uploadId) {
    var response = client.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/tags")
                    .queryParam("image_upload_id", uploadId)
                    .build())
            .retrieve()
            .toEntity(String.class);
    if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
      throw new HTTPException(response.getStatusCode().value());
    }

    var jsonObject = new JSONObject(response.getBody());
    if (jsonObject.has("result")) {
      var tags = jsonObject.getJSONObject("result").getJSONArray("tags");

      var topTags = new String[3];
      for (var i = 0; i < 3 && i < tags.length(); i++) {
        topTags[i] = tags.getJSONObject(i).getJSONObject("tag").getString("en");
      }

      return topTags;
    }

    return null;
  }

  private boolean isRetryableException(Throwable throwable) {
    if (throwable.getCause() instanceof HTTPException) {
      int statusCode = ((HTTPException) throwable.getCause()).getStatusCode();
      return statusCode == 429 || statusCode >= 500;
    }
    return false;
  }
}
