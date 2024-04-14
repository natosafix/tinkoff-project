package org.example.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.domain.Image;

@Schema(requiredProperties = {"images"}, type = "object")
@Data
@AllArgsConstructor
public class GetImagesResponse {

    @Schema(type = "array", description = "Список изображений")
    Image[] images;
}
