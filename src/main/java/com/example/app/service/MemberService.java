package com.example.app.service;

import com.example.app.dto.MemberRequest;
import com.example.app.entity.Member;
import com.example.app.entity.MembershipStatus;
import com.example.app.entity.Trainer;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.kafka.GymEventProducer;
import com.example.app.repository.MemberRepository;
import com.example.app.repository.TrainerRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

  @Autowired private MemberRepository memberRepository;

  @Autowired private TrainerRepository trainerRepository;

  @Autowired private GymEventProducer eventProducer;

  public Page<Member> list(Pageable pageable) {
    return memberRepository.findAll(pageable);
  }

  public Page<Member> search(String query, Pageable pageable) {
    return memberRepository
        .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            query, query, query, pageable);
  }

  public Member get(Long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
  }

  @Transactional
  public Member create(MemberRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Member email already exists");
    }
    Member member = new Member();
    member.setName(request.getName());
    member.setEmail(request.getEmail());
    member.setPhone(request.getPhone());
    member.setMembershipStartDate(
        request.getMembershipStartDate() != null
            ? request.getMembershipStartDate()
            : LocalDate.now());
    member.setStatus(MembershipStatus.ACTIVE);
    if (request.getTrainerId() != null) {
      Trainer trainer =
          trainerRepository
              .findById(request.getTrainerId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Trainer not found: " + request.getTrainerId()));
      member.setTrainer(trainer);
    }
    Member saved = memberRepository.save(member);
    eventProducer.publish(
        "MEMBER_REGISTERED",
        "{\"memberId\":" + saved.getId() + ",\"email\":\"" + saved.getEmail() + "\"}");
    return saved;
  }

  @Transactional
  public Member update(Long id, MemberRequest request) {
    Member member = get(id);
    member.setName(request.getName());
    member.setEmail(request.getEmail());
    member.setPhone(request.getPhone());
    if (request.getTrainerId() != null) {
      Trainer trainer =
          trainerRepository
              .findById(request.getTrainerId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Trainer not found: " + request.getTrainerId()));
      member.setTrainer(trainer);
    }
    return memberRepository.save(member);
  }

  @Transactional
  public Member assignTrainer(Long memberId, Long trainerId) {
    Member member = get(memberId);
    Trainer trainer =
        trainerRepository
            .findById(trainerId)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + trainerId));
    member.setTrainer(trainer);
    return memberRepository.save(member);
  }

  @Transactional
  public void delete(Long id) {
    Member member = get(id);
    memberRepository.delete(member);
  }
}
