package com.example.app.service;

import com.example.app.dto.SubscriptionRequest;
import com.example.app.entity.Member;
import com.example.app.entity.MembershipPlan;
import com.example.app.entity.MembershipStatus;
import com.example.app.entity.Subscription;
import com.example.app.entity.SubscriptionStatus;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.kafka.GymEventProducer;
import com.example.app.repository.MemberRepository;
import com.example.app.repository.MembershipPlanRepository;
import com.example.app.repository.SubscriptionRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

  @Autowired private SubscriptionRepository subscriptionRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MembershipPlanRepository planRepository;

  @Autowired private GymEventProducer eventProducer;

  public Page<Subscription> history(Long memberId, Pageable pageable) {
    return subscriptionRepository.findByMemberId(memberId, pageable);
  }

  public Subscription current(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    return subscriptionRepository
        .findFirstByMemberAndStatusOrderByEndDateDesc(member, SubscriptionStatus.ACTIVE)
        .orElseThrow(
            () -> new ResourceNotFoundException("No active subscription for member: " + memberId));
  }

  @Transactional
  public Subscription assign(SubscriptionRequest request) {
    Member member =
        memberRepository
            .findById(request.getMemberId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));
    MembershipPlan plan =
        planRepository
            .findById(request.getPlanId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Plan not found: " + request.getPlanId()));
    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(plan.getDurationDays());
    Subscription subscription = new Subscription();
    subscription.setMember(member);
    subscription.setPlan(plan);
    subscription.setStartDate(start);
    subscription.setEndDate(end);
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    Subscription saved = subscriptionRepository.save(subscription);
    member.setMembershipStartDate(start);
    member.setMembershipExpiryDate(end);
    member.setStatus(MembershipStatus.ACTIVE);
    memberRepository.save(member);
    eventProducer.publish(
        "SUBSCRIPTION_ASSIGNED",
        "{\"subscriptionId\":" + saved.getId() + ",\"memberId\":" + member.getId() + "}");
    return saved;
  }

  @Transactional
  public Subscription renew(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    Subscription last =
        subscriptionRepository
            .findFirstByMemberAndStatusOrderByEndDateDesc(member, SubscriptionStatus.ACTIVE)
            .orElseGet(
                () ->
                    subscriptionRepository
                        .findFirstByMemberAndStatusOrderByEndDateDesc(
                            member, SubscriptionStatus.EXPIRED)
                        .orElseThrow(
                            () ->
                                new BadRequestException(
                                    "No previous subscription to renew for member: " + memberId)));
    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(last.getPlan().getDurationDays());
    Subscription renewed = new Subscription();
    renewed.setMember(member);
    renewed.setPlan(last.getPlan());
    renewed.setStartDate(start);
    renewed.setEndDate(end);
    renewed.setStatus(SubscriptionStatus.ACTIVE);
    Subscription saved = subscriptionRepository.save(renewed);
    member.setMembershipStartDate(start);
    member.setMembershipExpiryDate(end);
    member.setStatus(MembershipStatus.ACTIVE);
    memberRepository.save(member);
    eventProducer.publish(
        "SUBSCRIPTION_RENEWED",
        "{\"subscriptionId\":" + saved.getId() + ",\"memberId\":" + member.getId() + "}");
    return saved;
  }

  @Transactional
  public Subscription cancel(Long subscriptionId) {
    Subscription subscription =
        subscriptionRepository
            .findById(subscriptionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
    subscription.setStatus(SubscriptionStatus.CANCELLED);
    Subscription saved = subscriptionRepository.save(subscription);
    eventProducer.publish("SUBSCRIPTION_CANCELLED", "{\"subscriptionId\":" + saved.getId() + "}");
    return saved;
  }
}
