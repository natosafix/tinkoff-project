package org.example.exceptions;

public class ImageFiltersRequestNotFoundException extends BaseNotFoundException {
  public ImageFiltersRequestNotFoundException(String requestId) {
    super("Не найден запрос на применение фильтров с id=" + requestId);
  }
}
