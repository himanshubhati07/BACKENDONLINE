package com.example.app.service;

import com.example.app.document.User;
import com.example.app.dto.JwtResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.dto.UserResponse;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import com.example.app.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       UserDetailsServiceImpl userDetailsService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException("Email already registered");
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("USER"));
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User saved = repository.save(user);
        log.info("User registration completed for user id {}", saved.getId());
        return UserService.toResponse(saved);
    }

    public JwtResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = repository.findByEmailIgnoreCase(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Rejected login attempt");
            throw new BadCredentialsException("Invalid credentials");
        }
        UserDetails details = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(details);
        log.info("Successful login for user id {}", user.getId());
        return new JwtResponse(true, "Login successful", token, "Bearer", jwtUtil.getExpirationSeconds(),
                UserService.toResponse(user));
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(); }
}
