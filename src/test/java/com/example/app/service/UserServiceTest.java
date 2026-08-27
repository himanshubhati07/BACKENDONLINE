package com.example.app.service;

import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.dto.PageResponse;
import com.example.app.dto.UserResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User sampleUser() {
        return User.builder()
                .id("user-1")
                .name("John Doe")
                .email("john@example.com")
                .password("hashed")
                .role(Role.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getCurrentUser_authenticated_returnsProfileWithoutPassword() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john@example.com", null, List.of()));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser()));

        UserResponse response = userService.getCurrentUser();

        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getName()).isEqualTo("John Doe");
    }

    @Test
    void getCurrentUser_notFound_throwsResourceNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghost@example.com", null, List.of()));
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getAllUsers_returnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(sampleUser()));
        when(userRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserResponse> response = userService.getAllUsers(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_existing_returnsResponse() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(sampleUser()));

        UserResponse response = userService.getUserById("user-1");

        assertThat(response.getId()).isEqualTo("user-1");
    }

    @Test
    void getUserById_missing_throwsResourceNotFoundException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("missing"));
    }

    @Test
    void deleteUser_existing_deletesIt() {
        User user = sampleUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        userService.deleteUser("user-1");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_missing_throwsResourceNotFoundException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("missing"));
    }
}
