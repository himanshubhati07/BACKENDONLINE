package com.example.app.controller;

import com.example.app.dto.TrainerRequest;
import com.example.app.entity.Trainer;
import com.example.app.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainers", description = "Trainer management")
public class TrainerController {

  @Autowired private TrainerService trainerService;

  @GetMapping
  @Operation(summary = "List trainers (paginated)")
  public ResponseEntity<Page<Trainer>> list(@PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(trainerService.list(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a trainer by id")
  public ResponseEntity<Trainer> get(@PathVariable Long id) {
    return ResponseEntity.ok(trainerService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add a trainer (admin only)")
  public ResponseEntity<Trainer> create(@Valid @RequestBody TrainerRequest request) {
    return ResponseEntity.ok(trainerService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a trainer (admin only)")
  public ResponseEntity<Trainer> update(
      @PathVariable Long id, @Valid @RequestBody TrainerRequest request) {
    return ResponseEntity.ok(trainerService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a trainer (admin only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    trainerService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
