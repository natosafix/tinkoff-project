package org.example.dtos;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(requiredProperties = {"imageId",}, type = "object")
@Data
@AllArgsConstructor
public class UploadImageResponse implements Serializable {

    @Schema(type = "string", format = "uuid", description = "ИД файла")
    private String imageId;
}
