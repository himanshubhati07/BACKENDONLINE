package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiEntryRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    private String name;

    @NotBlank(message = "Endpoint is required")
    @Size(max = 255, message = "Endpoint must be at most 255 characters")
    private String endpoint;

    @NotBlank(message = "Method is required")
    @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE", message = "Method must be one of GET, POST, PUT, PATCH, DELETE")
    private String method;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
