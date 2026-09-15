package com.example.app.controller;

import com.example.app.dto.AuthDtos;
import com.example.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a user")
  public ResponseEntity<AuthDtos.UserResponse> register(
      @Valid @RequestBody AuthDtos.RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
  }

  @PostMapping("/login")
  @Operation(summary = "Log in and receive a JWT")
  public ResponseEntity<AuthDtos.JwtResponse> login(
      @Valid @RequestBody AuthDtos.LoginRequest request) {
    return ResponseEntity.ok(service.login(request));
  }
}
