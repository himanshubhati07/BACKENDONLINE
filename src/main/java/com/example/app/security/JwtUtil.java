package com.example.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final Key key;

  public JwtUtil(@Value("${JWT_SECRET}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1800000))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractEmail(String token) {
    return claims(token).getSubject();
  }

  public boolean isValid(String token) {
    try {
      claims(token);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  private Claims claims(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}
