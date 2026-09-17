package com.example.app.controller;

import com.example.app.dto.ChangePasswordRequest;
import com.example.app.dto.JwtResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RefreshRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.dto.UpdateProfileRequest;
import com.example.app.dto.UserProfileResponse;
import com.example.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, refresh, and profile endpoints")
public class AuthController {

  @Autowired private AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register a new user")
  public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  @Operation(summary = "Login with email and password")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Exchange a valid refresh token for a new access token")
  public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
  }

  @PostMapping("/logout")
  @Operation(summary = "Revoke a refresh token")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
    authService.logout(request.getRefreshToken());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  @Operation(summary = "Get the logged-in user's profile")
  public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
    return ResponseEntity.ok(authService.getProfile(authentication.getName()));
  }

  @PutMapping("/me")
  @Operation(summary = "Update the logged-in user's profile")
  public ResponseEntity<UserProfileResponse> updateMe(
      Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
  }

  @PutMapping("/me/password")
  @Operation(summary = "Change the logged-in user's password")
  public ResponseEntity<Void> changePassword(
      Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(authentication.getName(), request);
    return ResponseEntity.noContent().build();
  }
}
