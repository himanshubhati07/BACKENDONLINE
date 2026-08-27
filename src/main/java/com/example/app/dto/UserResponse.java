package com.example.app.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(String id, String name, String email, Set<String> roles,
                           Instant createdAt, Instant updatedAt) {}
