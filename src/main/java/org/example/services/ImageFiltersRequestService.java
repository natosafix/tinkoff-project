package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.domain.*;
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

    public ImageFiltersRequest get(String requestId, String sourceImageId, String username) {
        var user = userService.getUserByUsername(username);
        var image = imageService.getImage(sourceImageId);
        checkUserAccess(user.getId(), image);
        return repository.findById(requestId).orElseThrow(() -> new ImageFiltersRequestNotFoundException(requestId));
    }

    public ImageFiltersRequest apply(String sourceImageId, String[] filters, String username) {
        var requestId = UUID.randomUUID().toString();
        var user = userService.getUserByUsername(username);
        var image = imageService.getImage(sourceImageId);
        checkUserAccess(user.getId(), image);
        var request = repository.save(new ImageFiltersRequest().setRequestId(requestId).setSourceImage(image).setStatus(Status.WIP));
        kafkaProducer.send(new KafkaImageFiltersRequest(sourceImageId,  requestId, Arrays.stream(filters).map(f -> Filter.valueOf(f)).toArray(Filter[]::new)));
        return request;
    }

    private void checkUserAccess(int currentUserId, Image image) {
        var imageOwnerId = image.getUser().getId();

        if (!imageOwnerId.equals(currentUserId)) {
            throw new ImageNotFoundException(image.getImageId());
        }
    }
}
