package com.example.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-expiration-ms}")
  private long accessExpirationMs;

  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  private Key signingKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateAccessToken(String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessExpirationMs);
    return Jwts.builder()
        .setSubject(email)
        .claim("type", "access")
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(signingKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String email, UUID jti) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshExpirationMs);
    return Jwts.builder()
        .setSubject(email)
        .setId(jti.toString())
        .claim("type", "refresh")
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(signingKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public long getRefreshExpirationMs() {
    return refreshExpirationMs;
  }

  public Claims parseClaims(String token) throws ExpiredJwtException {
    return Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token).getBody();
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractType(String token) {
    return (String) parseClaims(token).get("type");
  }
}
