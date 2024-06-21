package org.example;


import org.example.kafka.ProcessedImageId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository with filter processed image.
 */
public interface ProcessedImageRepository extends
        JpaRepository<ProcessedImage, ProcessedImageId> {
}
