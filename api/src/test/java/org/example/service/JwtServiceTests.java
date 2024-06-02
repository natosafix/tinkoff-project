package org.example.service;

import io.jsonwebtoken.Claims;
import org.example.config.PostgreTestConfig;
import org.example.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(initializers = PostgreTestConfig.Initializer.class)
public class JwtServiceTests extends ServiceTestsBase {

  @Autowired
  private JwtService jwtService;

  @Test
  void getUsernameFromToken_ShouldReturnUsername() {
    var userGiven = getTestUser();
    var token = jwtService.generateToken(userGiven);

    var username = jwtService.getUsernameFromToken(token);

    assertEquals(userGiven.getUsername(), username);
  }

  @Test
  void getRoleFromToken_ShouldReturnRole() {
    var userGiven = getTestUser();
    var token = jwtService.generateToken(userGiven);

    var role = jwtService.getRoleFromToken(token);

    assertEquals(userGiven.getRole().toString(), role);
  }

  @Test
  public void getClaim_ShouldReturnClaim() {
    var userGiven = getTestUser();
    var token = jwtService.generateToken(userGiven);

    var claimValue = jwtService.getClaim(token, Claims::getSubject);

    assertEquals(userGiven.getUsername(), claimValue);
  }

  @Test
  public void generateToken_ShouldGenerateValidToken() {
    var user = getTestUser();

    var token = jwtService.generateToken(user);

    assertEquals(user.getUsername(), jwtService.getUsernameFromToken(token));
    assertEquals(user.getRole().toString(), jwtService.getRoleFromToken(token));
    assertTrue(jwtService.isTokenValid(token, user));
  }
}
