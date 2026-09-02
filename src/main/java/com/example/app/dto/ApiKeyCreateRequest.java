package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApiKeyCreateRequest(
    @NotBlank(message = "API key name is required")
        @Size(max = 100, message = "API key name must be at most 100 characters")
        String name) {}
