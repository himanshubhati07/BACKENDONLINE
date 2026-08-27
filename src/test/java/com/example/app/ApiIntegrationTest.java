package com.example.app;

import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    private static final String EMAIL = "integration.test@example.com";
    @Autowired MockMvc mvc;
    @Autowired UserRepository repository;

    @AfterEach
    void cleanup() { repository.findByEmailIgnoreCase(EMAIL).ifPresent(repository::delete); }

    @Test
    void registerLoginCrudAndValidationFlow() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Integration Test\",\"email\":\"" + EMAIL + "\",\"password\":\"Test@123\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id", not(blankString())))
                .andExpect(jsonPath("$.email").value(EMAIL)).andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Integration Test\",\"email\":\"" + EMAIL + "\",\"password\":\"Test@123\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"Test@123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token", not(blankString())))
                .andExpect(jsonPath("$.success").value(true));

        String body = mvc.perform(get("/api/v1/users").param("filter", "integration.test")
                        .param("limit", "20").param("sortBy", "email").param("direction", "asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].email").value(EMAIL))
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mvc.perform(put("/api/v1/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Integration Updated\",\"email\":\"" + EMAIL + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Integration Updated"));
        mvc.perform(get("/api/v1/users/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(delete("/api/v1/users/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/users/{id}", id)).andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"bad\",\"password\":\"weak\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validationErrors.email").exists());
    }
}
