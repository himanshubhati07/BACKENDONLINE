package com.example.app.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, long totalElements, int offset, int limit) {}
