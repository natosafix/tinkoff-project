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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ImageResource {
  private final ImageService imageService;
  private final ImageMapper imageMapper;
  private final JwtService jwtService;

  /**
   * Upload image.
   *
   * @param file image
   * @param bearerToken jwt token
   * @return UploadImageResponse
   * @throws Exception when some went wrong
   */
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
  public UploadImageResponse uploadImage(
          @RequestParam MultipartFile file,
          @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
    var jwtToken = bearerToken.substring("Bearer ".length());
    var authorUsername = jwtService.getUsernameFromToken(jwtToken);
    return imageMapper.imageToUploadImageResponse(imageService.uploadImage(file, authorUsername));
  }

  /**
   * Download image.
   *
   * @param imageId image id
   * @param bearerToken jwt token
   * @return image bytes in string utf-8 format
   * @throws Exception when some went wrong
   */
  @Operation(summary = "Скачивание файла по ИД", operationId = "downloadImage")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                  content = @Content(mediaType = MediaType.ALL_VALUE)),
          @ApiResponse(responseCode = "404",
                  description = "Файл не найден в системе или недоступен",
                  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                          schema = @Schema(implementation = UiSuccessContainer.class))),
          @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                          schema = @Schema(implementation = UiSuccessContainer.class)))
        })

  @GetMapping(value = "/image/{image-id}", produces = {MediaType.IMAGE_PNG_VALUE,
          MediaType.IMAGE_JPEG_VALUE})
  public byte[] downloadImage(
          @PathVariable("image-id") UUID imageId,
          @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
    var jwtToken = bearerToken.substring("Bearer ".length());
    var authorUsername = jwtService.getUsernameFromToken(jwtToken);

    return imageService.downloadImage(imageId.toString(), authorUsername);
  }

  /**
   * Delete image.
   *
   * @param imageId image id
   * @param bearerToken jwt token
   * @return UiSuccessContainer
   * @throws Exception when some went wrong
   */
  @Operation(summary = "Удаление файла по ИД", operationId = "deleteImage")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                          schema = @Schema(implementation = UiSuccessContainer.class))),
          @ApiResponse(responseCode = "404",
                  description = "Файл не найден в системе или недоступен",
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

    imageService.deleteImage(imageId, authorUsername);

    return new UiSuccessContainer();
  }

  /**
   * Get user images.
   *
   * @param bearerToken jwt token
   * @return GetImagesResponse
   */
  @Operation(summary = "Получение списка изображений, которые доступны пользователю",
          operationId = "getImages")
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
