package org.example.service;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.exceptions.UserNotFoundException;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTests extends ServiceTestsBase {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void getUserByUsername_ShouldReturnUser() {
    var user = getTestUser();

    var foundUser = userService.getUserByUsername(user.getUsername());

    Assertions.assertEquals(user.getId(), foundUser.getId());
    Assertions.assertEquals(user.getUsername(), foundUser.getUsername());
    Assertions.assertEquals(user.getPassword(), foundUser.getPassword());
    Assertions.assertEquals(user.getRole(), foundUser.getRole());
    Assertions.assertEquals(user.getIsDeleted(), foundUser.getIsDeleted());
  }

  @Test
  public void getUserByUsername_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
    var username = UUID.randomUUID().toString();

    Executable action = () -> userService.getUserByUsername(username);

    assertThrows(UserNotFoundException.class, action, "Не найден пользователь с username=" + username);
  }

  @Test
  public void getUserByUsername_ShouldThrowEntityNotFoundException_WhenUserIsDeleted() {
    var user = new User()
            .setUsername(UUID.randomUUID().toString())
            .setPassword(UUID.randomUUID().toString())
            .setRole(UserRole.USER)
            .setIsDeleted(true);
    userRepository.save(user);

    Executable action = () -> userService.getUserByUsername(user.getUsername());

    assertThrows(UserNotFoundException.class, action, "Не найден пользователь с username=" + user.getUsername());
  }
}
