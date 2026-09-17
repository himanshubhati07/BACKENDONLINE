package com.example.app.controller;

import com.example.app.dto.WorkoutPlanRequest;
import com.example.app.entity.WorkoutPlan;
import com.example.app.service.WorkoutPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/workout-plans")
@Tag(name = "Workout Plans", description = "Workout plans and exercises for members")
public class WorkoutPlanController {

  @Autowired private WorkoutPlanService workoutPlanService;

  @GetMapping
  @Operation(summary = "List workout plans for a member (paginated)")
  public ResponseEntity<Page<WorkoutPlan>> list(
      @RequestParam Long memberId, @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(workoutPlanService.list(memberId, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a workout plan by id")
  public ResponseEntity<WorkoutPlan> get(@PathVariable Long id) {
    return ResponseEntity.ok(workoutPlanService.get(id));
  }

  @PostMapping
  @Operation(summary = "Create a workout plan for a member")
  public ResponseEntity<WorkoutPlan> create(@Valid @RequestBody WorkoutPlanRequest request) {
    return ResponseEntity.ok(workoutPlanService.create(request));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a workout plan")
  public ResponseEntity<WorkoutPlan> update(
      @PathVariable Long id, @Valid @RequestBody WorkoutPlanRequest request) {
    return ResponseEntity.ok(workoutPlanService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a workout plan")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    workoutPlanService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
