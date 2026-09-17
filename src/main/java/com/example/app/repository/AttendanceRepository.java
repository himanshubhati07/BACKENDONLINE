package com.example.app.repository;

import com.example.app.entity.Attendance;
import com.example.app.entity.Member;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  Optional<Attendance> findFirstByMemberAndCheckOutTimeIsNull(Member member);

  Page<Attendance> findByMemberId(Long memberId, Pageable pageable);

  Page<Attendance> findByCheckInTimeBetween(Instant start, Instant end, Pageable pageable);

  long countByCheckInTimeBetween(Instant start, Instant end);
}
