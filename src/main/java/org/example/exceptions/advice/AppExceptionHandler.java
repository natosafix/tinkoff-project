package org.example.exceptions.advice;

import org.example.dtos.UiSuccessContainer;
import org.example.exceptions.BadRequestException;
import org.example.exceptions.BaseNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.expression.AccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class AppExceptionHandler {
  @Value("${minio.image-size}")
  private int maxImageSize;

  @ExceptionHandler(BaseNotFoundException.class)
  public ResponseEntity<UiSuccessContainer> handleEntityNotFoundException(
          BaseNotFoundException notFoundException) {
    return handleException(HttpStatus.NOT_FOUND, notFoundException.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<UiSuccessContainer> handleDataIntegrityViolationException(
          DataIntegrityViolationException dataIntegrityViolationException) {
    return handleException(HttpStatus.CONFLICT,
            dataIntegrityViolationException.getCause().getLocalizedMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<UiSuccessContainer> handleIllegalArgumentException(
          IllegalArgumentException illegalArgumentException) {
    return handleException(HttpStatus.BAD_REQUEST,
            illegalArgumentException.getCause().getLocalizedMessage());
  }

  @ExceptionHandler(AccessException.class)
  public ResponseEntity<UiSuccessContainer> handleAccessException(AccessException exception) {
    return handleException(HttpStatus.FORBIDDEN, "Доступ запрещен: " + exception.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<UiSuccessContainer> handleBadRequestException(
          BadRequestException exception) {
    return handleException(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<UiSuccessContainer> handleMaxUploadSizeExceededException() {
    return handleException(HttpStatus.BAD_REQUEST,
            "Размер файла превышает " + maxImageSize / 1024 / 1024 + "МБ");
  }

  private ResponseEntity<UiSuccessContainer> handleException(HttpStatusCode status,
                                                             String exceptionMessage) {
    var body = new UiSuccessContainer(false, exceptionMessage);
    return new ResponseEntity<>(body, status);
  }
}
