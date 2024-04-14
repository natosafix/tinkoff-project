package org.example.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dtos.GetImagesResponse;
import org.example.dtos.UiSuccessContainer;
import org.example.dtos.UploadImageResponse;
import org.example.mapper.ImageMapper;
import org.example.services.ImageService;
import org.example.services.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@Tag(name = "ImageController", description = "Базовый CRUD API для работы с картинками")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ImageResource {
    private final ImageService imageService;
    private final ImageMapper imageMapper;
    private final JwtService jwtService;

    @Operation(summary = "Загрузка нового изображения в систему", operationId = "uploadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UploadImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Файл не прошел валидацию",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadImageResponse uploadImage(@RequestParam MultipartFile file,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);

        return imageMapper.imageToUploadImageResponse(imageService.uploadImage(file, authorUsername));
    }

    @Operation(summary = "Скачивание файла по ИД", operationId = "downloadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.ALL_VALUE)),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/image/{image-id}")
    public String downloadImage(@PathVariable("image-id") UUID imageId,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);

        return new String(imageService.downloadImage(imageId.toString(), authorUsername), StandardCharsets.UTF_8);
    }

    @Operation(summary = "Удаление файла по ИД", operationId = "deleteImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @DeleteMapping(value = "/image/{image-id}")
    public UiSuccessContainer deleteImage(@PathVariable("image-id") String imageId,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION)
                                          String bearerToken) throws Exception {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);
        var imageIdUuid = UUID.fromString(imageId);

        imageService.deleteImage(imageIdUuid.toString(), authorUsername);

        return new UiSuccessContainer();
    }

    @Operation(summary = "Получение списка изображений, которые доступны пользователю", operationId = "getImages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetImagesResponse.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/images")
    public GetImagesResponse getImages(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);

        return imageMapper.imagesToGetImagesResponse(imageService.getUserImages(authorUsername));
    }
}
