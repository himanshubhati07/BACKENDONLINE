package com.example.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:c29tZS1zdXBlci1zZWNyZXQta2V5LWZvci1qd3QtaG1hYy1zaGEyNTYtYWxnb3JpdGhtLXVzZS1vbmx5}") String secret,
            @Value("${jwt.expiration-ms:1800000}") long expirationMs) {
        String normalizedSecret = (secret == null || secret.isBlank())
                ? "c29tZS1zdXBlci1zZWNyZXQta2V5LWZvci1qd3QtaG1hYy1zaGEyNTYtYWxnb3JpdGhtLXVzZS1vbmx5"
                : secret;
        byte[] keyBytes;
        try {
            keyBytes = java.util.Base64.getDecoder().decode(normalizedSecret);
            if (keyBytes.length < 32) {
                keyBytes = normalizedSecret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException ex) {
            keyBytes = normalizedSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            // pad to satisfy HS256 minimum key length requirement
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String email) {
        try {
            final String extracted = extractEmail(token);
            return extracted.equals(email) && !isTokenExpired(token);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
