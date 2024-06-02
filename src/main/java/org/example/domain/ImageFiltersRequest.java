package org.example.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "image_filters_requests")
@Accessors(chain = true)
public class ImageFiltersRequest {

    @Id
    @Column(name = "request_id")
    private UUID requestId;

    @ManyToOne
    @JoinColumn(name = "source_image_id", referencedColumnName = "id")
    private Image sourceImage;

    @ManyToOne
    @JoinColumn(name = "filtered_image_id", referencedColumnName = "id")
    private Image filteredImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
