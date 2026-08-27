package com.example.app.controller;

import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.AuthResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.PageResponse;
import com.example.app.dto.RegisterRequest;
import com.example.app.dto.UserResponse;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<String> createdEmails = new ArrayList<>();

    @BeforeEach
    void useApacheHttpClient() {
        // Avoids "cannot retry due to server authentication, in streaming mode" on
        // requests that deliberately expect a 401/403 response.
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/api/v1" + path;
    }

    @AfterEach
    void cleanup() {
        createdEmails.forEach(email -> userRepository.findByEmail(email).ifPresent(userRepository::delete));
        createdEmails.clear();
    }

    private String register(String name, String email, String password) {
        createdEmails.add(email);
        restTemplate.postForEntity(baseUrl("/auth/register"), new RegisterRequest(name, email, password), String.class);
        return login(email, password);
    }

    private String login(String email, String password) {
        ResponseEntity<ApiResponse<AuthResponse>> response = restTemplate.exchange(
                baseUrl("/auth/login"), HttpMethod.POST,
                new HttpEntity<>(new LoginRequest(email, password)),
                new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                });
        return response.getBody().getData().getToken();
    }

    private String createAdmin(String email, String password) {
        createdEmails.add(email);
        User admin = User.builder()
                .name("User IT Admin")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepository.save(admin);
        return login(email, password);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void getCurrentUser_validToken_returnsProfile() {
        String email = "user-it-" + UUID.randomUUID() + "@example.com";
        String token = register("Current User", email, "Password@123");

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl("/users/me"), HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<ApiResponse<UserResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getEmail()).isEqualTo(email);
        assertThat(response.getBody().getData().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void getCurrentUser_noToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl("/users/me"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getCurrentUser_invalidToken_returns401() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/users/me"), HttpMethod.GET,
                new HttpEntity<>(authHeaders("this.is.not.a.valid.jwt")), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAllUsers_asAdmin_returnsPagedList() {
        String email = "user-it-admin-" + UUID.randomUUID() + "@example.com";
        String token = createAdmin(email, "AdminPass@123");

        ResponseEntity<ApiResponse<PageResponse<UserResponse>>> response = restTemplate.exchange(
                baseUrl("/users?page=0&size=20"), HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<ApiResponse<PageResponse<UserResponse>>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getSize()).isEqualTo(20);
    }

    @Test
    void getAllUsers_asUser_returns403() {
        String email = "user-it-plain-" + UUID.randomUUID() + "@example.com";
        String token = register("Plain User", email, "Password@123");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/users?page=0&size=20"), HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllUsers_noToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl("/users?page=0&size=20"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deleteUser_asAdmin_removesUserThenSecondDeleteReturns404() {
        String adminEmail = "user-it-admin2-" + UUID.randomUUID() + "@example.com";
        String adminToken = createAdmin(adminEmail, "AdminPass@123");

        String victimEmail = "user-it-victim-" + UUID.randomUUID() + "@example.com";
        register("Victim User", victimEmail, "Password@123");
        String victimId = userRepository.findByEmail(victimEmail).orElseThrow().getId();

        ResponseEntity<ApiResponse<Object>> deleteResponse = restTemplate.exchange(
                baseUrl("/users/" + victimId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(adminToken)),
                new ParameterizedTypeReference<ApiResponse<Object>>() {
                });
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> secondDelete = restTemplate.exchange(
                baseUrl("/users/" + victimId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(adminToken)), String.class);
        assertThat(secondDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_asUser_returns403() {
        String email = "user-it-nonadmin-" + UUID.randomUUID() + "@example.com";
        String token = register("Plain User", email, "Password@123");
        String userId = userRepository.findByEmail(email).orElseThrow().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/users/" + userId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
