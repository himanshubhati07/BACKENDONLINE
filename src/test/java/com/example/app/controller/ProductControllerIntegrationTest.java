package com.example.app.controller;

import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.AuthResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.PageResponse;
import com.example.app.dto.ProductRequest;
import com.example.app.dto.ProductResponse;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<String> createdEmails = new ArrayList<>();
    private final List<String> createdProductIds = new ArrayList<>();

    private String adminToken;
    private String userToken;
    private String createdProductId;
    private final String category = "test-cat-" + UUID.randomUUID();
    private final String productName = "Integration Phone " + UUID.randomUUID();

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/api/v1" + path;
    }

    @BeforeEach
    void useApacheHttpClient() {
        // Avoids "cannot retry due to server authentication, in streaming mode" on
        // POST/PUT/DELETE requests that deliberately expect a 401/403 response.
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @BeforeAll
    void setUpUsers() {
        String adminEmail = "product-it-admin-" + UUID.randomUUID() + "@example.com";
        createdEmails.add(adminEmail);
        User admin = User.builder()
                .name("Product IT Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass@123"))
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepository.save(admin);
        adminToken = login(adminEmail, "AdminPass@123");

        String userEmail = "product-it-user-" + UUID.randomUUID() + "@example.com";
        createdEmails.add(userEmail);
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new com.example.app.dto.RegisterRequest("Product IT User", userEmail, "UserPass@123"), String.class);
        userToken = login(userEmail, "UserPass@123");
    }

    @AfterAll
    void cleanup() {
        createdProductIds.forEach(id -> productRepository.findById(id).ifPresent(productRepository::delete));
        createdEmails.forEach(email -> userRepository.findByEmail(email).ifPresent(userRepository::delete));
    }

    private String login(String email, String password) {
        ResponseEntity<ApiResponse<AuthResponse>> response = restTemplate.exchange(
                baseUrl("/auth/login"), HttpMethod.POST,
                new HttpEntity<>(new LoginRequest(email, password)),
                new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                });
        return response.getBody().getData().getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @Order(1)
    void createProduct_asAdmin_returns201() {
        ProductRequest request = new ProductRequest();
        request.setName(productName);
        request.setDescription("A phone used for integration testing");
        request.setPrice(new BigDecimal("299.99"));
        request.setCategory(category);
        request.setQuantity(15);

        ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
                baseUrl("/products"), HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(adminToken)),
                new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getName()).isEqualTo(productName);
        assertThat(response.getBody().getData().getPrice()).isEqualTo(new BigDecimal("299.99"));
        createdProductId = response.getBody().getData().getId();
        createdProductIds.add(createdProductId);
        assertThat(createdProductId).isNotBlank();
    }

    @Test
    @Order(2)
    void createProduct_asUser_returns403() {
        ProductRequest request = new ProductRequest();
        request.setName("Forbidden Product");
        request.setPrice(new BigDecimal("10.00"));
        request.setCategory(category);
        request.setQuantity(1);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products"), HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(userToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(3)
    void createProduct_noToken_returns401() {
        ProductRequest request = new ProductRequest();
        request.setName("Unauth Product");
        request.setPrice(new BigDecimal("10.00"));
        request.setCategory(category);
        request.setQuantity(1);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl("/products"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void createProduct_invalidPrice_returns400() {
        ProductRequest request = new ProductRequest();
        request.setName("Bad Price Product");
        request.setPrice(new BigDecimal("-5"));
        request.setCategory(category);
        request.setQuantity(1);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products"), HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(adminToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(5)
    void createProduct_invalidQuantity_returns400() {
        ProductRequest request = new ProductRequest();
        request.setName("Bad Quantity Product");
        request.setPrice(new BigDecimal("5.00"));
        request.setCategory(category);
        request.setQuantity(-1);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products"), HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(adminToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(6)
    void getProducts_noToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/products?page=0&size=20"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    void getProducts_withToken_returnsPagedList() {
        ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> response = restTemplate.exchange(
                baseUrl("/products?page=0&size=20"), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)),
                new ParameterizedTypeReference<ApiResponse<PageResponse<ProductResponse>>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getSize()).isEqualTo(20);
        assertThat(response.getBody().getData().getContent()).isNotNull();
    }

    @Test
    @Order(8)
    void getProductById_existing_returnsCorrectFields() {
        ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)),
                new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getId()).isEqualTo(createdProductId);
        assertThat(response.getBody().getData().getName()).isEqualTo(productName);
    }

    @Test
    @Order(9)
    void getProductById_missing_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products/does-not-exist-" + UUID.randomUUID()), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(10)
    void searchProducts_byName_returnsMatchingProduct() {
        String searchTerm = productName.split(" ")[1];
        ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> response = restTemplate.exchange(
                baseUrl("/products?search=" + searchTerm), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)),
                new ParameterizedTypeReference<ApiResponse<PageResponse<ProductResponse>>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent())
                .anyMatch(p -> p.getId().equals(createdProductId));
    }

    @Test
    @Order(11)
    void filterProducts_byCategoryAndSort_returnsFilteredResult() {
        ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> response = restTemplate.exchange(
                baseUrl("/products?category=" + category + "&sort=price,asc"), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)),
                new ParameterizedTypeReference<ApiResponse<PageResponse<ProductResponse>>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent())
                .allMatch(p -> p.getCategory().equalsIgnoreCase(category));
    }

    @Test
    @Order(12)
    void searchProducts_noMatches_returnsEmptyContent() {
        ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> response = restTemplate.exchange(
                baseUrl("/products?search=no-such-product-" + UUID.randomUUID()), HttpMethod.GET,
                new HttpEntity<>(authHeaders(userToken)),
                new ParameterizedTypeReference<ApiResponse<PageResponse<ProductResponse>>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).isEmpty();
        assertThat(response.getBody().getData().getTotalElements()).isZero();
    }

    @Test
    @Order(13)
    void updateProduct_asUser_returns403() {
        ProductRequest request = new ProductRequest();
        request.setName(productName);
        request.setPrice(new BigDecimal("399.99"));
        request.setCategory(category);
        request.setQuantity(20);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.PUT,
                new HttpEntity<>(request, authHeaders(userToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(14)
    void updateProduct_asAdmin_returns200AndReflectsChanges() {
        ProductRequest request = new ProductRequest();
        request.setName(productName + " Updated");
        request.setDescription("updated description");
        request.setPrice(new BigDecimal("399.99"));
        request.setCategory(category);
        request.setQuantity(20);

        ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.PUT,
                new HttpEntity<>(request, authHeaders(adminToken)),
                new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getName()).isEqualTo(productName + " Updated");
        assertThat(response.getBody().getData().getPrice()).isEqualTo(new BigDecimal("399.99"));
        assertThat(response.getBody().getData().getQuantity()).isEqualTo(20);
    }

    @Test
    @Order(15)
    void deleteProduct_asUser_returns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(userToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(16)
    void deleteProduct_asAdmin_returns200ThenGetReturns404() {
        ResponseEntity<ApiResponse<Object>> deleteResponse = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(adminToken)),
                new ParameterizedTypeReference<ApiResponse<Object>>() {
                });
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate.exchange(
                baseUrl("/products/" + createdProductId), HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)), String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        createdProductIds.remove(createdProductId);
    }
}
