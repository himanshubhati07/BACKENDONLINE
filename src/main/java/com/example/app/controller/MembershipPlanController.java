package com.example.app.controller;

import com.example.app.dto.MembershipPlanRequest;
import com.example.app.entity.MembershipPlan;
import com.example.app.service.MembershipPlanService;
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
@RequestMapping("/api/v1/plans")
@Tag(name = "Membership Plans", description = "Membership plan management")
public class MembershipPlanController {

  @Autowired private MembershipPlanService planService;

  @GetMapping
  @Operation(summary = "List membership plans (paginated)")
  public ResponseEntity<Page<MembershipPlan>> list(@PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(planService.list(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a membership plan by id")
  public ResponseEntity<MembershipPlan> get(@PathVariable Long id) {
    return ResponseEntity.ok(planService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add a membership plan (admin only)")
  public ResponseEntity<MembershipPlan> create(@Valid @RequestBody MembershipPlanRequest request) {
    return ResponseEntity.ok(planService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a membership plan (admin only)")
  public ResponseEntity<MembershipPlan> update(
      @PathVariable Long id, @Valid @RequestBody MembershipPlanRequest request) {
    return ResponseEntity.ok(planService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a membership plan (admin only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    planService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
