package com.example.app.controller;

import com.example.app.dto.ApiKeyCreateRequest;
import com.example.app.dto.ApiKeyCreatedResponse;
import com.example.app.dto.ApiKeyResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.security.ApiKey;
import com.example.app.security.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/api-keys")
@Tag(name = "API Key Administration")
public class ApiKeyController {

  private final ApiKeyRepository apiKeyRepository;
  private final String configuredKey;

  public ApiKeyController(
      ApiKeyRepository apiKeyRepository, @Value("${admin.api-key}") String configuredKey) {
    this.apiKeyRepository = apiKeyRepository;
    this.configuredKey = configuredKey;
  }

  @PostMapping
  @Operation(summary = "Create an API key using the administrator key")
  public ResponseEntity<ApiKeyCreatedResponse> create(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
      @Valid @RequestBody ApiKeyCreateRequest request) {
    authorize(adminKey);
    String rawKey = UUID.randomUUID() + "-" + UUID.randomUUID();
    ApiKey apiKey = new ApiKey();
    apiKey.setName(request.name().trim());
    apiKey.setKeyHash(sha256(rawKey));
    ApiKey saved = apiKeyRepository.save(apiKey);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiKeyCreatedResponse(saved.getId(), saved.getName(), rawKey));
  }

  @GetMapping
  @Operation(summary = "List API key metadata using the administrator key")
  public ResponseEntity<List<ApiKeyResponse>> list(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
    authorize(adminKey);
    List<ApiKeyResponse> response =
        apiKeyRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(
                key ->
                    new ApiKeyResponse(
                        key.getId(),
                        key.getName(),
                        key.isActive(),
                        key.getCreatedAt(),
                        key.getLastUsedAt()))
            .toList();
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Revoke an API key using the administrator key")
  public ResponseEntity<Void> revoke(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
      @PathVariable Long id) {
    authorize(adminKey);
    ApiKey apiKey =
        apiKeyRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
    apiKey.setActive(false);
    apiKeyRepository.save(apiKey);
    return ResponseEntity.noContent().build();
  }

  private void authorize(String adminKey) {
    if (adminKey == null
        || !MessageDigest.isEqual(
            adminKey.getBytes(StandardCharsets.UTF_8),
            configuredKey.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid administrator key");
    }
  }

  private String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
