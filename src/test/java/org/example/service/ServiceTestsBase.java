package org.example.service;

import org.example.config.PostgreTestConfig;
import org.example.domain.Image;
import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.repositories.ImageRepository;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

@SpringBootTest
@ContextConfiguration(initializers = PostgreTestConfig.Initializer.class)
public class ServiceTestsBase {
    @Autowired
    protected ImageRepository imageRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected UserService userService;

    @BeforeEach
    @AfterEach
    protected void clear() {
        imageRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User getTestUser() {
        var user = new User()
                .setUsername(UUID.randomUUID().toString())
                .setPassword(UUID.randomUUID().toString())
                .setRole(UserRole.USER)
                .setIsDeleted(false);

        return userRepository.save(user);
    }
}
