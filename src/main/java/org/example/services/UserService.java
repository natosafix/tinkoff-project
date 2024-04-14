package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.domain.User;
import org.example.exceptions.UserNotFoundException;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  /**
   * Get user by username.
   *
   * @param username username
   * @return user
   */
  public User getUserByUsername(String username) {
    var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

    if (user.getIsDeleted()) {
      throw new UserNotFoundException(username);
    }

    return user;
  }
}
