package org.example.services;

import com.google.gson.Gson;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.example.domain.Filter;
import org.example.domain.Image;
import org.example.domain.ImageFiltersRequest;
import org.example.domain.Status;
import org.example.exceptions.BadRequestException;
import org.example.exceptions.ImageFiltersRequestNotFoundException;
import org.example.exceptions.ImageNotFoundException;
import org.example.kafka.KafkaImageFiltersRequest;
import org.example.kafka.KafkaProducer;
import org.example.repositories.ImageFiltersRequestRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageFiltersRequestService {
  private final ImageFiltersRequestRepository repository;
  private final UserService userService;
  private final ImageService imageService;
  private final KafkaProducer kafkaProducer;
  private final Bucket bucket;

  /**
   * Get ImageFiltersRequest.
   *
   * @param requestId requestId
   * @param sourceImageId sourceImageId
   * @param username username
   *
   * @return ImageFiltersRequest
   */
  public ImageFiltersRequest get(String requestId, String sourceImageId, String username) {
    var user = userService.getUserByUsername(username);
    var image = imageService.getImage(sourceImageId);
    checkUserAccess(user.getId(), image);
    return repository.findById(UUID.fromString(requestId))
            .orElseThrow(() -> new ImageFiltersRequestNotFoundException(requestId));
  }

  /**
   * Apply ImageFiltersRequest.
   *
   * @param sourceImageId sourceImageId
   * @param filters filters
   * @param username username
   *
   * @return ImageFiltersRequest
   */
  public ImageFiltersRequest apply(String sourceImageId, String[] filters, String username) {
    var filtersArray = Arrays.stream(filters).map(Filter::valueOf).toArray(Filter[]::new);

    if (Arrays.stream(filtersArray).anyMatch(f -> f == Filter.Immaga)) {
      var canFilterImage = bucket.tryConsume(1);
      if (!canFilterImage)
        throw new BadRequestException("Превышен допустимы лимит запросов фильтра Immaga");
    }

    var requestId = UUID.randomUUID();
    var user = userService.getUserByUsername(username);
    var image = imageService.getImage(sourceImageId);
    checkUserAccess(user.getId(), image);
    var request = new ImageFiltersRequest()
            .setRequestId(requestId)
            .setSourceImage(image)
            .setStatus(Status.WIP);
    request = repository.save(request);
    var kafkaRequest = new KafkaImageFiltersRequest(
            UUID.fromString(sourceImageId),
            requestId,
            filtersArray);
    kafkaProducer.sendWip(new Gson().toJson(kafkaRequest));
    return request;
  }

  private void checkUserAccess(int currentUserId, Image image) {
    var imageOwnerId = image.getUser().getId();

    if (!imageOwnerId.equals(currentUserId)) {
      throw new ImageNotFoundException(image.getImageId().toString());
    }
  }
}
