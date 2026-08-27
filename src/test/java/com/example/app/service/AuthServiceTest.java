package com.example.app.service;

import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.dto.AuthResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.InvalidCredentialsException;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void register_newEmail_savesHashedPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password@123");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("user-1");
            return u;
        });
        when(jwtUtil.generateToken(eq("user-1"), eq("john@example.com"), eq("USER"))).thenReturn("signed-jwt");
        when(jwtUtil.getExpirationSeconds()).thenReturn(1800L);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("signed-jwt");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(1800L);
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        // password must never be persisted in plaintext
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
        assertThat(captor.getValue().getPassword()).isNotEqualTo("Password@123");
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Jane", "jane@example.com", "Password@123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");
        User user = User.builder()
                .id("user-1")
                .name("John Doe")
                .email("john@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken("user-1", "john@example.com", "USER")).thenReturn("signed-jwt");
        when(jwtUtil.getExpirationSeconds()).thenReturn(1800L);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("signed-jwt");
        assertThat(response.getUser().getId()).isEqualTo("user-1");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword@1");
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword@1", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("nobody@example.com", "Password@123");
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
