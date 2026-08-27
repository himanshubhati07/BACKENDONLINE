package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.dto.AuthResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<String> createdEmails = new ArrayList<>();

    @BeforeEach
    void useApacheHttpClient() {
        // The default JDK HttpURLConnection-based request factory throws
        // "cannot retry due to server authentication, in streaming mode" when a
        // POST request deliberately receives a 401 response. Apache HttpClient5
        // does not have this limitation.
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

    private String uniqueEmail() {
        String email = "auth-it-" + UUID.randomUUID() + "@example.com";
        createdEmails.add(email);
        return email;
    }

    @Test
    void register_validPayload_returns201WithTokenAndUser() {
        RegisterRequest request = new RegisterRequest("Integration User", uniqueEmail(), "Password@123");

        ResponseEntity<ApiResponse<AuthResponse>> response = restTemplate.exchange(
                baseUrl("/auth/register"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(request),
                new org.springframework.core.ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getToken()).isNotBlank();
        assertThat(response.getBody().getData().getUser().getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void register_duplicateEmail_returns409() {
        RegisterRequest request = new RegisterRequest("Integration User", uniqueEmail(), "Password@123");
        restTemplate.postForEntity(baseUrl("/auth/register"), request, String.class);

        ResponseEntity<String> second = restTemplate.postForEntity(baseUrl("/auth/register"), request, String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody()).contains("DUPLICATE_RESOURCE");
    }

    @Test
    void register_invalidEmailFormat_returns400() {
        RegisterRequest request = new RegisterRequest("Integration User", "not-an-email", "Password@123");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl("/auth/register"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("VALIDATION_ERROR");
    }

    @Test
    void register_weakPassword_returns400() {
        RegisterRequest request = new RegisterRequest("Integration User", uniqueEmail(), "weak");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl("/auth/register"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_missingName_returns400() {
        RegisterRequest request = new RegisterRequest("", uniqueEmail(), "Password@123");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl("/auth/register"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_validCredentials_returns200WithToken() {
        String email = uniqueEmail();
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Integration User", email, "Password@123"), String.class);

        ResponseEntity<ApiResponse<AuthResponse>> response = restTemplate.exchange(
                baseUrl("/auth/login"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(new LoginRequest(email, "Password@123")),
                new org.springframework.core.ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getToken()).isNotBlank();
        assertThat(response.getBody().getData().getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_invalidPassword_returns401() {
        String email = uniqueEmail();
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Integration User", email, "Password@123"), String.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/auth/login"), new LoginRequest(email, "WrongPassword@1"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownEmail_returns401() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/auth/login"), new LoginRequest("unknown-" + UUID.randomUUID() + "@example.com", "Password@123"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_missingFields_returns400() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/auth/login"), new LoginRequest("", ""), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
