package org.example.exceptions;

public class UserNotFoundException extends BaseNotFoundException {
  public UserNotFoundException(String username) {
    super("Не найден пользователь с username=" + username);
  }
}
