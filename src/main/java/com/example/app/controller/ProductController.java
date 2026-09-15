package com.example.app.controller;

import com.example.app.dto.ProductDtos;
import com.example.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
public class ProductController {
  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Create a product")
  public ResponseEntity<ProductDtos.ProductResponse> create(
      @Valid @RequestBody ProductDtos.ProductRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @GetMapping
  @Operation(summary = "List products")
  public ResponseEntity<Page<ProductDtos.ProductResponse>> all(
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(service.all(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID")
  public ResponseEntity<ProductDtos.ProductResponse> one(@PathVariable Long id) {
    return ResponseEntity.ok(service.one(id));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a product")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
