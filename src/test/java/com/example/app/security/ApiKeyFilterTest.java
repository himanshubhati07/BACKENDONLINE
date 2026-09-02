package com.example.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiKeyFilterTest {

  @Test
  void rejectsRequestWithoutApiKey() throws Exception {
    ApiKeyRepository repository = mock(ApiKeyRepository.class);
    ApiKeyFilter filter = new ApiKeyFilter(repository);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(
        new MockHttpServletRequest("GET", "/api/v1/users"), response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("API key is required");
  }

  @Test
  void permitsRequestWithConfiguredEnvironmentApiKey() throws Exception {
    ApiKeyRepository repository = mock(ApiKeyRepository.class);
    ApiKeyFilter filter = new ApiKeyFilter(repository, "environment-api-key");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
    request.addHeader("X-API-Key", "environment-api-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test
  void permitsRequestWithActiveApiKey() throws Exception {
    ApiKeyRepository repository = mock(ApiKeyRepository.class);
    ApiKeyFilter filter = new ApiKeyFilter(repository);
    ApiKey key = new ApiKey();
    key.setLastUsedAt(Instant.EPOCH);
    String rawKey = "test-api-key";
    when(repository.findByKeyHashAndActiveTrue(hash(rawKey))).thenReturn(Optional.of(key));

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
    request.addHeader("X-API-Key", rawKey);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(chain.getRequest()).isNotNull();
    verify(repository).save(any(ApiKey.class));
  }

  private String hash(String value) throws Exception {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }
}
