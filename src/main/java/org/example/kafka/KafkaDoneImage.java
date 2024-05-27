package org.example.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaDoneImage implements Serializable {
    private String imageId;
    private String requestId;
}
