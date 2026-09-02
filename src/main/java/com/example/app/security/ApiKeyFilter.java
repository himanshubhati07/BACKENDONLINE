package com.example.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);
  private final ApiKeyRepository apiKeyRepository;

  public ApiKeyFilter(ApiKeyRepository apiKeyRepository) {
    this.apiKeyRepository = apiKeyRepository;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/actuator/health")
        || path.startsWith("/api/public/")
        || path.startsWith("/api/v1/api-keys")
        || path.equals("/docs")
        || path.startsWith("/swagger-ui")
        || path.equals("/api-docs")
        || path.startsWith("/api-docs/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String rawKey = request.getHeader("X-API-Key");
    if (rawKey == null || rawKey.isBlank()) {
      reject(response, "API key is required");
      return;
    }
    String hash = sha256(rawKey);
    ApiKey apiKey = apiKeyRepository.findByKeyHashAndActiveTrue(hash).orElse(null);
    if (apiKey == null) {
      LOGGER.warn("API key authentication failed for request path {}", request.getRequestURI());
      reject(response, "Invalid API key");
      return;
    }
    apiKey.setLastUsedAt(Instant.now());
    apiKeyRepository.save(apiKey);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "api-key-" + apiKey.getId(), null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  private String sha256(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private void reject(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response
        .getWriter()
        .write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
  }
}
