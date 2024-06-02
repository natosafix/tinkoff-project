package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;

  /**
   * Register user.
   *
   * @param user user
   * @return jwt token
   */
  public String register(User user) {
    encodeUserPassword(user);
    user.setRole(UserRole.USER);
    user.setIsDeleted(false);
    userRepository.save(user);
    return jwtService.generateToken(user);
  }

  /**
   * Login user.
   *
   * @param user user
   * @return jwt token
   */
  public String authenticate(User user) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword()
            )
    );
    var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
    return jwtService.generateToken(userDetails);
  }

  private void encodeUserPassword(User user) {
    var userPassword = user.getPassword();
    user.setPassword(passwordEncoder.encode(userPassword));
  }
}

