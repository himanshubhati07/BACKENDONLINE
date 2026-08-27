package com.example.app.dto;

public record JwtResponse(boolean success, String message, String token, String tokenType,
                          long expiresIn, UserResponse user) {}
