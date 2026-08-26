package com.example.app.dto;

import com.example.app.document.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;
}
