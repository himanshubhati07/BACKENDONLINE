package com.example.app.dto;

import com.example.app.entity.UserStatus;
import java.time.Instant;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt) {}
