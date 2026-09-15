package com.example.app.service;

import com.example.app.dto.AuthDtos;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @Transactional
  public AuthDtos.UserResponse register(AuthDtos.RegisterRequest request) {
    if (users.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email is already registered");
    }
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(encoder.encode(request.password()));
    User saved = users.save(user);
    return response(saved);
  }

  public AuthDtos.JwtResponse login(AuthDtos.LoginRequest request) {
    User user =
        users
            .findByEmail(request.email())
            .orElseThrow(() -> new SecurityException("Invalid email or password"));
    if (!encoder.matches(request.password(), user.getPassword())) {
      throw new SecurityException("Invalid email or password");
    }
    return new AuthDtos.JwtResponse(jwt.generateToken(user.getEmail()), response(user));
  }

  private AuthDtos.UserResponse response(User user) {
    return new AuthDtos.UserResponse(user.getId(), user.getName(), user.getEmail());
  }
}
