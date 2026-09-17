package com.example.app.repository;

import com.example.app.entity.WorkoutPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

  Page<WorkoutPlan> findByMemberId(Long memberId, Pageable pageable);
}
