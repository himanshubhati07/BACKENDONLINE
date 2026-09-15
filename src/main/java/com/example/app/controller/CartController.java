package com.example.app.controller;

import com.example.app.dto.CartDtos;
import com.example.app.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart")
public class CartController {
  private final CartService service;

  public CartController(CartService service) {
    this.service = service;
  }

  @PostMapping("/items")
  @Operation(summary = "Add a product to the authenticated user's cart")
  public ResponseEntity<CartDtos.CartResponse> add(
      Authentication authentication, @Valid @RequestBody CartDtos.CartItemRequest request) {
    return ResponseEntity.ok(service.add(authentication.getName(), request));
  }

  @GetMapping
  @Operation(summary = "Get the authenticated user's cart")
  public ResponseEntity<CartDtos.CartResponse> get(Authentication authentication) {
    return ResponseEntity.ok(service.get(authentication.getName()));
  }

  @PutMapping("/items/{productId}")
  @Operation(summary = "Update a cart item quantity")
  public ResponseEntity<CartDtos.CartResponse> update(
      Authentication authentication,
      @PathVariable Long productId,
      @Valid @RequestBody CartDtos.QuantityRequest request) {
    return ResponseEntity.ok(service.update(authentication.getName(), productId, request));
  }

  @DeleteMapping("/items/{productId}")
  @Operation(summary = "Remove an item from the cart")
  public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long productId) {
    service.delete(authentication.getName(), productId);
    return ResponseEntity.noContent().build();
  }
}
