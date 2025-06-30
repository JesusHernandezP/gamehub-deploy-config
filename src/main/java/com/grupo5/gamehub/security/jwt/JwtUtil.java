package com.grupo5.gamehub.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

  @Value("${jwt.secret:defaultSecretKeyForDevelopmentAndTestingWhichShouldBeAtLeast32BytesLong}")
  private String secret;

  @Value("${jwt.expiration.ms:3600000}")
  private long expirationMs;

  // Genera un token JWT para un usuario
  public String generateToken(String username) {
    return JWT.create()
            .withSubject(username)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
            .sign(Algorithm.HMAC256(secret));
  }

  // Valida un token JWT y extrae el nombre de usuario
  public String validateTokenAndRetrieveSubject(String token) {
    try {
      DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
              .build()
              .verify(token);
      return decodedJWT.getSubject();
    } catch (JWTVerificationException exception){
      return null;
    }
  }
}