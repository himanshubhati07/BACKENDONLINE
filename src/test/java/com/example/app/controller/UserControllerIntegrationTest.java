package com.example.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.dto.UserRequest;
import com.example.app.dto.UserResponse;
import com.example.app.entity.UserStatus;
import com.example.app.exception.GlobalExceptionHandler;
import com.example.app.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerIntegrationTest {

  private MockMvc mockMvc;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(new UserController(userService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void performsUserCrudOverHttp() throws Exception {
    UserResponse created = response(1L, "Ada", "Lovelace", "ada@example.com");
    when(userService.create(any(UserRequest.class))).thenReturn(created);
    when(userService.get(1L)).thenReturn(created);
    when(userService.update(eq(1L), any(UserRequest.class))).thenReturn(created);
    doNothing().when(userService).delete(1L);

    String payload =
        """
{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","phone":"1234567890","status":"ACTIVE"}
""";
    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("ada@example.com"));
    mockMvc
        .perform(get("/api/v1/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Ada"));
    mockMvc
        .perform(put("/api/v1/users/1").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isOk());
    mockMvc.perform(delete("/api/v1/users/1")).andExpect(status().isNoContent());
  }

  @Test
  void rejectsInvalidUserPayload() throws Exception {
    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }

  private UserResponse response(Long id, String firstName, String lastName, String email) {
    return new UserResponse(
        id,
        firstName,
        lastName,
        email,
        "1234567890",
        UserStatus.ACTIVE,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
