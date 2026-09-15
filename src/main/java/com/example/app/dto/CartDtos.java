package com.example.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {
  private CartDtos() {}

  public record CartItemRequest(@NotNull Long productId, @NotNull @Positive Integer quantity) {}

  public record QuantityRequest(@NotNull @Positive Integer quantity) {}

  public record CartItemResponse(
      Long id,
      Long productId,
      String productName,
      BigDecimal unitPrice,
      Integer quantity,
      BigDecimal subtotal) {}

  public record CartResponse(Long id, List<CartItemResponse> items, BigDecimal totalAmount) {}
}
