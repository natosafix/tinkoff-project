package org.example.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.domain.Filter;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class KafkaImageFiltersRequest implements Serializable {
    private String imageId;
    private UUID requestId;
    private Filter[] filters;
}
