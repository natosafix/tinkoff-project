package org.example.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(requiredProperties = {"requestId"}, type = "object")
@Data
@AllArgsConstructor
public class GetModifiedImageByRequestIdResponse {

    @Schema(type = "string", format = "uuid", description = "ИД модифицированного или оригинального файла в случае отсутствия первого")
    String imageId;

    @Schema(type = "string", format = "uuid", description = "Статус обработки файла")
    String status;
}
