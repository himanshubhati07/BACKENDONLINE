package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEntryResponse {

    private String id;
    private String name;
    private String endpoint;
    private String method;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
