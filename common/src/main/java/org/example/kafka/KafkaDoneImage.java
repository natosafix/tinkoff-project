package org.example.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class KafkaDoneImage implements Serializable {
  private String imageId;
  private UUID requestId;
}