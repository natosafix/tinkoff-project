package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(requiredProperties = {"success"}, type = "object")
@Data
@AllArgsConstructor
public class UiSuccessContainer {

  @Schema(type = "boolean", description = "Признак успеха")
  boolean success;

  @Schema(type = "string", description = "Сообщение об ошибке")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String message;

  public UiSuccessContainer() {
    success = true;
    message = "Операция выполнена успешно";
  }
}
