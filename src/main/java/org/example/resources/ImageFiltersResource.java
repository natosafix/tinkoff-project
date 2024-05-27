package org.example.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dtos.ApplyImageFiltersResponse;
import org.example.dtos.GetModifiedImageByRequestIdResponse;
import org.example.dtos.UiSuccessContainer;
import org.example.mapper.ImageFiltersRequestMapper;
import org.example.services.ImageFiltersRequestService;
import org.example.services.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Image Filters Controller", description = "Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ImageFiltersResource {
    private final JwtService jwtService;
    private final ImageFiltersRequestService service;
    private final ImageFiltersRequestMapper mapper;

    /**
     * Apply filters to image
     *
     * @param imageId image id
     * @param filters filters
     * @param bearerToken jwt token
     * @return ApplyImageFiltersResponse
     * @throws Exception when some went wrong
     */
    @Operation(summary = "Применение указанных фильтров к изображению", operationId = "applyImageFilters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApplyImageFiltersResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })

    @PostMapping(value = "/image/{image-id}/filters/apply")
    public ApplyImageFiltersResponse applyImageFilters(
            @PathVariable("image-id") String imageId,
            @RequestParam String[] filters,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);
        return mapper.imageFiltersRequestToApplyImageFiltersResponse(service.apply(imageId, filters, authorUsername));
    }

    /**
     * Get filters request status
     *
     * @param imageId source image
     * @param requestId filters request id
     * @param bearerToken jwt token
     * @return GetModifiedImageByRequestIdResponse
     * @throws Exception when some went wrong
     */
    @Operation(summary = "Получение ИД измененного файла по ИД запроса", operationId = "getModifiedImageByRequestId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetModifiedImageByRequestIdResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })

    @GetMapping(value = "/image/{image-id}/filters/{request-id}")
    public GetModifiedImageByRequestIdResponse getModifiedImageByRequestId(
            @PathVariable("image-id") String imageId,
            @PathVariable("request-id") String requestId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) throws Exception {
        var jwtToken = bearerToken.substring("Bearer ".length());
        var authorUsername = jwtService.getUsernameFromToken(jwtToken);
        return mapper.imageFiltersRequestToGetModifiedImageByRequestId(service.get(requestId, imageId, authorUsername));
    }
}
