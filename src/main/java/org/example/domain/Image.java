package org.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.UUID;

@Schema(requiredProperties = {"filename", "size"}, type = "object")
@Data
@Entity
@Table(name = "images")
@Accessors(chain = true)
public class Image implements Serializable {
  @Schema(type = "string", format = "uuid", description = "ИД файла")
  @Id
  @Column(name = "id")
  private UUID imageId;

  @Schema(type = "string", description = "Название изображения")
  @Column(name = "filename", length = 100)
  private String filename;

  @Schema(type = "integer", format = "int32", description = "Размер файла в байтах")
  @Column(name = "size")
  private Long size;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;
}
