package org.example;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.kafka.ProcessedImageId;

import java.util.UUID;

@Entity
@Table(name = "blur_processed_images")
@Data
@Accessors(chain = true)
@IdClass(ProcessedImageId.class)
public class ProcessedImage {

  @Id
  private UUID requestId;
  @Id
  private UUID imageId;
}
