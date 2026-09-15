package com.example.app.kafka;

import java.time.Instant;

public record CartItemEvent(
    String eventType,
    Long userId,
    Long cartItemId,
    Long productId,
    Integer quantity,
    Instant timestamp) {}
