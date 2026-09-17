package com.example.app.service;

import com.example.app.dto.ExerciseRequest;
import com.example.app.dto.WorkoutPlanRequest;
import com.example.app.entity.Exercise;
import com.example.app.entity.Member;
import com.example.app.entity.WorkoutPlan;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.MemberRepository;
import com.example.app.repository.WorkoutPlanRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutPlanService {

  @Autowired private WorkoutPlanRepository workoutPlanRepository;

  @Autowired private MemberRepository memberRepository;

  public Page<WorkoutPlan> list(Long memberId, Pageable pageable) {
    return workoutPlanRepository.findByMemberId(memberId, pageable);
  }

  public WorkoutPlan get(Long id) {
    return workoutPlanRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found: " + id));
  }

  @Transactional
  public WorkoutPlan create(WorkoutPlanRequest request) {
    Member member =
        memberRepository
            .findById(request.getMemberId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));
    WorkoutPlan plan = new WorkoutPlan();
    plan.setMember(member);
    plan.setName(request.getName());
    plan.setDescription(request.getDescription());
    plan.setExercises(toExercises(request.getExercises(), plan));
    return workoutPlanRepository.save(plan);
  }

  @Transactional
  public WorkoutPlan update(Long id, WorkoutPlanRequest request) {
    WorkoutPlan plan = get(id);
    plan.setName(request.getName());
    plan.setDescription(request.getDescription());
    plan.getExercises().clear();
    plan.getExercises().addAll(toExercises(request.getExercises(), plan));
    return workoutPlanRepository.save(plan);
  }

  @Transactional
  public void delete(Long id) {
    WorkoutPlan plan = get(id);
    workoutPlanRepository.delete(plan);
  }

  private List<Exercise> toExercises(List<ExerciseRequest> requests, WorkoutPlan plan) {
    List<Exercise> exercises = new ArrayList<>();
    if (requests == null) {
      return exercises;
    }
    for (ExerciseRequest req : requests) {
      Exercise exercise = new Exercise();
      exercise.setWorkoutPlan(plan);
      exercise.setName(req.getName());
      exercise.setSets(req.getSets());
      exercise.setRepetitions(req.getRepetitions());
      exercise.setDurationMinutes(req.getDurationMinutes());
      exercises.add(exercise);
    }
    return exercises;
  }
}
