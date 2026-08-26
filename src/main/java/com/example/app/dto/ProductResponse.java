package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer quantity;
    private Instant createdAt;
    private Instant updatedAt;
}
