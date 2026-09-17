package com.example.app.service;

import com.example.app.dto.ChangePasswordRequest;
import com.example.app.dto.JwtResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.dto.UpdateProfileRequest;
import com.example.app.dto.UserProfileResponse;
import com.example.app.entity.RefreshToken;
import com.example.app.entity.Role;
import com.example.app.entity.User;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.kafka.GymEventProducer;
import com.example.app.repository.RefreshTokenRepository;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  @Autowired private UserRepository userRepository;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private GymEventProducer eventProducer;

  @Transactional
  public JwtResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email is already registered");
    }
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setPhone(request.getPhone());
    Role role = Role.MEMBER;
    if (request.getRole() != null && !request.getRole().isBlank()) {
      try {
        role = Role.valueOf(request.getRole().toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Invalid role: " + request.getRole());
      }
    }
    user.setRole(role);
    User saved = userRepository.save(user);
    eventProducer.publish(
        "USER_REGISTERED",
        "{\"userId\":" + saved.getId() + ",\"email\":\"" + saved.getEmail() + "\"}");
    return buildTokens(saved);
  }

  public JwtResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new org.springframework.security.authentication.BadCredentialsException(
                        "Invalid credentials"));
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Invalid credentials");
    }
    return buildTokens(user);
  }

  @Transactional
  public JwtResponse refresh(String refreshToken) {
    Claims claims;
    try {
      claims = jwtUtil.parseClaims(refreshToken);
    } catch (Exception ex) {
      throw new BadRequestException("Invalid or expired refresh token");
    }
    String type = (String) claims.get("type");
    if (!"refresh".equals(type)) {
      throw new BadRequestException("Provided token is not a refresh token");
    }
    UUID jti = UUID.fromString(claims.getId());
    RefreshToken stored =
        refreshTokenRepository
            .findByJti(jti)
            .orElseThrow(() -> new BadRequestException("Refresh token not recognized"));
    if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
      throw new BadRequestException("Refresh token is revoked or expired");
    }
    User user =
        userRepository
            .findById(stored.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
    JwtResponse response = new JwtResponse();
    response.setToken(newAccessToken);
    response.setRefreshToken(refreshToken);
    response.setUserId(user.getId());
    response.setEmail(user.getEmail());
    response.setRole(user.getRole().name());
    return response;
  }

  @Transactional
  public void logout(String refreshToken) {
    Claims claims;
    try {
      claims = jwtUtil.parseClaims(refreshToken);
    } catch (Exception ex) {
      throw new BadRequestException("Invalid refresh token");
    }
    UUID jti = UUID.fromString(claims.getId());
    RefreshToken stored =
        refreshTokenRepository
            .findByJti(jti)
            .orElseThrow(() -> new BadRequestException("Refresh token not recognized"));
    stored.setRevoked(true);
    refreshTokenRepository.save(stored);
  }

  public UserProfileResponse getProfile(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return toProfile(user);
  }

  @Transactional
  public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (request.getName() != null && !request.getName().isBlank()) {
      user.setName(request.getName());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }
    User saved = userRepository.save(user);
    return toProfile(saved);
  }

  @Transactional
  public void changePassword(String email, ChangePasswordRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new BadRequestException("Old password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  private JwtResponse buildTokens(User user) {
    String accessToken = jwtUtil.generateAccessToken(user.getEmail());
    UUID jti = UUID.randomUUID();
    String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), jti);
    RefreshToken tokenEntity = new RefreshToken();
    tokenEntity.setUserId(user.getId());
    tokenEntity.setJti(jti);
    tokenEntity.setExpiresAt(Instant.now().plusMillis(jwtUtil.getRefreshExpirationMs()));
    tokenEntity.setRevoked(false);
    refreshTokenRepository.save(tokenEntity);
    return new JwtResponse(
        accessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name());
  }

  private UserProfileResponse toProfile(User user) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setPhone(user.getPhone());
    response.setRole(user.getRole().name());
    return response;
  }
}
