package com.example.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanRequest {

  @NotNull private Long memberId;

  @NotBlank private String name;

  private String description;

  @Valid private List<ExerciseRequest> exercises = new ArrayList<>();

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ExerciseRequest> getExercises() {
    return exercises;
  }

  public void setExercises(List<ExerciseRequest> exercises) {
    this.exercises = exercises;
  }
}
