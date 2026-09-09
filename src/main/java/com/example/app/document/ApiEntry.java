package com.example.app.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "apis")
public class ApiEntry {

    @Id
    private String id;

    @Indexed
    private String name;

    private String endpoint;

    private String method;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;
}
