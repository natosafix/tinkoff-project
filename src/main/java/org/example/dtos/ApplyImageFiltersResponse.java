package org.example.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(requiredProperties = {"requestId"}, type = "object")
@Data
@AllArgsConstructor
public class ApplyImageFiltersResponse {

  @Schema(type = "string", format = "uuid", description = "ИД запроса в системе")
  String requestId;
}
