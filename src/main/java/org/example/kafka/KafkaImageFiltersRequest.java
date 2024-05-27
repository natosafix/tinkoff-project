package org.example.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.domain.Filter;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaImageFiltersRequest implements Serializable {
    private String imageId;
    private String requestId;
    private Filter[] filters;
}
