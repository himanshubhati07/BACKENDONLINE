package com.example.app.controller;

import com.example.app.dto.PageResponse;
import com.example.app.dto.UserRequest;
import com.example.app.dto.UserResponse;
import com.example.app.entity.UserStatus;
import com.example.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@SecurityRequirement(name = "ApiKeyAuth")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @Operation(summary = "Create a user")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a user by id")
  public ResponseEntity<UserResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(userService.get(id));
  }

  @GetMapping
  @Operation(summary = "List users with offset pagination, sorting, status filtering, and search")
  public ResponseEntity<PageResponse<UserResponse>> list(
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "createdAt,desc") String sort,
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) String search) {
    return ResponseEntity.ok(userService.list(offset, limit, sort, status, search));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a user")
  public ResponseEntity<UserResponse> update(
      @PathVariable Long id, @Valid @RequestBody UserRequest request) {
    return ResponseEntity.ok(userService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Soft delete a user")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
