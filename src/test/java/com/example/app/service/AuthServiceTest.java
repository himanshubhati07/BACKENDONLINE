package com.example.app.service;

import com.example.app.document.User;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import com.example.app.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository repository;
    private AuthService service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        repository = mock(UserRepository.class);
        JwtUtil jwt = new JwtUtil("a-very-long-test-secret-with-at-least-32-characters", 1_800_000);
        service = new AuthService(repository, encoder, jwt, new UserDetailsServiceImpl(repository));
    }

    @Test
    void registrationHashesPasswordAndNormalizesEmail() {
        when(repository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId("id1"); return u; });
        var response = service.register(new RegisterRequest("Test User", " TEST@Example.com ", "Test@123"));
        assertEquals("test@example.com", response.email());
        assertNull(response.getClass().getRecordComponents()[0].getAccessor().getAnnotation(Deprecated.class));
        verify(repository).save(argThat(u -> !u.getPassword().equals("Test@123") && encoder.matches("Test@123", u.getPassword())));
    }

    @Test
    void duplicateRegistrationIsRejected() {
        when(repository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> service.register(new RegisterRequest("Test", "test@example.com", "Test@123")));
    }

    @Test
    void loginReturnsTokenButNoPassword() {
        User user = user();
        when(repository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        var response = service.login(new LoginRequest("test@example.com", "Test@123"));
        assertNotNull(response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("test@example.com", response.user().email());
    }

    @Test
    void invalidPasswordIsRejected() {
        when(repository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user()));
        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("test@example.com", "wrong")));
    }

    private User user() {
        User u = new User(); u.setId("id1"); u.setName("Test"); u.setEmail("test@example.com");
        u.setPassword(encoder.encode("Test@123")); u.setRoles(Set.of("USER"));
        u.setCreatedAt(Instant.now()); u.setUpdatedAt(Instant.now()); return u;
    }
}
