package com.example.app.dto;

import java.time.Instant;

public record ApiKeyResponse(
    Long id, String name, boolean active, Instant createdAt, Instant lastUsedAt) {}
