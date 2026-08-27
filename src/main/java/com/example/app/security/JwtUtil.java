package com.example.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:change-this-development-secret-key-before-production-32chars}") String secret,
                   @Value("${jwt.expiration-ms:1800000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(normalizeSecret(secret));
        this.expirationMs = expirationMs > 0 ? expirationMs : 1_800_000L;
    }

    private byte[] normalizeSecret(String secret) {
        String safeSecret = secret == null || secret.isBlank()
                ? "change-this-development-secret-key-before-production-32chars" : secret;
        byte[] bytes = safeSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) return bytes;
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            return "fallback-development-jwt-secret-key-32-bytes".getBytes(StandardCharsets.UTF_8);
        }
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) { return claims(token).getSubject(); }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = claims(token);
        return userDetails.getUsername().equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public long getExpirationSeconds() { return expirationMs / 1000; }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
