package com.example.app.service;

import com.example.app.dto.MembershipPlanRequest;
import com.example.app.entity.MembershipPlan;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.MembershipPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipPlanService {

  @Autowired private MembershipPlanRepository planRepository;

  public Page<MembershipPlan> list(Pageable pageable) {
    return planRepository.findAll(pageable);
  }

  public MembershipPlan get(Long id) {
    return planRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found: " + id));
  }

  @Transactional
  public MembershipPlan create(MembershipPlanRequest request) {
    MembershipPlan plan = new MembershipPlan();
    plan.setName(request.getName());
    plan.setDurationDays(request.getDurationDays());
    plan.setPrice(request.getPrice());
    plan.setDescription(request.getDescription());
    return planRepository.save(plan);
  }

  @Transactional
  public MembershipPlan update(Long id, MembershipPlanRequest request) {
    MembershipPlan plan = get(id);
    plan.setName(request.getName());
    plan.setDurationDays(request.getDurationDays());
    plan.setPrice(request.getPrice());
    plan.setDescription(request.getDescription());
    return planRepository.save(plan);
  }

  @Transactional
  public void delete(Long id) {
    MembershipPlan plan = get(id);
    planRepository.delete(plan);
  }
}
