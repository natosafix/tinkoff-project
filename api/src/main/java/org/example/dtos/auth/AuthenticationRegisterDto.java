package org.example.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class AuthenticationRegisterDto {
  private String username;
  private String password;
}