package org.example.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.exceptions.BadRequestException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtService {
  @Value("${application.auth.jwt.secret-key}")
  private String secretKey;

  @Value("${application.auth.jwt.expiration-timeout-milliseconds}")
  private Long expirationTimeoutMilliseconds;

  /**
   * Get username by token.
   *
   * @param jwtToken jwt token
   * @return username
   */
  public String getUsernameFromToken(String jwtToken) {
    return getClaim(jwtToken, Claims::getSubject);
  }

  /**
   * Get role by token.
   *
   * @param jwtToken jwt token
   * @return role
   */
  public String getRoleFromToken(String jwtToken) {
    return getClaim(jwtToken, claims -> claims.get("role", String.class)
            .substring("ROLE_".length()));
  }

  /**
   * Get claim.
   *
   * @param jwtToken jwt token
   * @param claimsResolver claims resolver
   * @param <T> claim type
   * @return claim
   */
  public <T> T getClaim(String jwtToken, @NotNull Function<Claims, T> claimsResolver) {
    var claims = getClaimsFromToken(jwtToken);

    return claimsResolver.apply(claims);
  }

  /**
   * Generate jwt token.
   *
   * @param userDetails user
   * @return jwt token
   */
  public String generateToken(UserDetails userDetails) {
    var extraClaims = new HashMap<String, Object>();
    extraClaims.put("role", userDetails.getAuthorities().stream().findFirst()
            .orElseThrow(() -> new RuntimeException("У пользователя нет ролей")).getAuthority());

    return generateToken(extraClaims, userDetails);
  }

  /**
   * Generate jwt token.
   *
   * @param extraClaims extra claims
   * @param userDetails user
   * @return jwt token
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts
            .builder()
            .claims()
            .add(extraClaims)
            .and()
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expirationTimeoutMilliseconds))
            .signWith(getSignInKey(), Jwts.SIG.HS256)
            .compact();
  }

  /**
   * Check is token valid.
   *
   * @param jwtToken jwt token
   * @param userDetails user
   * @return true or false
   */
  public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
    var username = getUsernameFromToken(jwtToken);

    return (username.equals(userDetails.getUsername())) && isTokenNotExpired(jwtToken);
  }

  private boolean isTokenNotExpired(String jwtToken) {
    return getTokenExpiration(jwtToken).after(new Date(System.currentTimeMillis()));
  }

  private Date getTokenExpiration(String jwtToken) {
    return getClaim(jwtToken, Claims::getExpiration);
  }

  private Claims getClaimsFromToken(String jwtToken) {
    try {
      return Jwts
              .parser()
              .verifyWith(getSignInKey())
              .build()
              .parseSignedClaims(jwtToken)
              .getPayload();
    } catch (Exception ex) {
      throw new BadRequestException("Invalid JWT");
    }
  }

  private SecretKey getSignInKey() {
    var keyBytes = Decoders.BASE64.decode(secretKey);

    return Keys.hmacShaKeyFor(keyBytes);
  }
}


