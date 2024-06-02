package org.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
