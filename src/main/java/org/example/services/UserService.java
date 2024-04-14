package org.example.services;

import org.example.domain.User;
import org.example.exceptions.UserNotFoundException;
import org.example.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserByUsername(String username) {
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (user.getIsDeleted())
            throw new UserNotFoundException(username);

        return user;
    }
}
