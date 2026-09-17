package com.example.app.service;

import com.example.app.entity.Attendance;
import com.example.app.entity.Member;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.kafka.GymEventProducer;
import com.example.app.repository.AttendanceRepository;
import com.example.app.repository.MemberRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

  @Autowired private AttendanceRepository attendanceRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private GymEventProducer eventProducer;

  @Transactional
  public Attendance checkIn(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    attendanceRepository
        .findFirstByMemberAndCheckOutTimeIsNull(member)
        .ifPresent(
            a -> {
              throw new BadRequestException(
                  "Member already checked in without checkout: attendance " + a.getId());
            });
    Attendance attendance = new Attendance();
    attendance.setMember(member);
    attendance.setCheckInTime(Instant.now());
    Attendance saved = attendanceRepository.save(attendance);
    eventProducer.publish(
        "MEMBER_CHECK_IN",
        "{\"attendanceId\":" + saved.getId() + ",\"memberId\":" + member.getId() + "}");
    return saved;
  }

  @Transactional
  public Attendance checkOut(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    Attendance attendance =
        attendanceRepository
            .findFirstByMemberAndCheckOutTimeIsNull(member)
            .orElseThrow(
                () -> new BadRequestException("No active check-in found for member: " + memberId));
    attendance.setCheckOutTime(Instant.now());
    Attendance saved = attendanceRepository.save(attendance);
    eventProducer.publish(
        "MEMBER_CHECK_OUT",
        "{\"attendanceId\":" + saved.getId() + ",\"memberId\":" + member.getId() + "}");
    return saved;
  }

  public Page<Attendance> today(Pageable pageable) {
    LocalDate today = LocalDate.now();
    Instant start = today.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return attendanceRepository.findByCheckInTimeBetween(start, end, pageable);
  }

  public Page<Attendance> history(Long memberId, Pageable pageable) {
    return attendanceRepository.findByMemberId(memberId, pageable);
  }
}
