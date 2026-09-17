package com.example.app.service;

import com.example.app.dto.DashboardResponse;
import com.example.app.entity.MembershipStatus;
import com.example.app.entity.PaymentStatus;
import com.example.app.repository.AttendanceRepository;
import com.example.app.repository.MemberRepository;
import com.example.app.repository.PaymentRepository;
import com.example.app.repository.TrainerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  @Autowired private MemberRepository memberRepository;

  @Autowired private TrainerRepository trainerRepository;

  @Autowired private AttendanceRepository attendanceRepository;

  @Autowired private PaymentRepository paymentRepository;

  public DashboardResponse summary() {
    DashboardResponse response = new DashboardResponse();
    response.setTotalMembers(memberRepository.count());
    response.setActiveMembers(
        memberRepository.findAll().stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .count());
    response.setExpiredMemberships(
        memberRepository.findAll().stream()
            .filter(m -> m.getStatus() == MembershipStatus.EXPIRED)
            .count());
    response.setTotalTrainers(trainerRepository.count());
    LocalDate today = LocalDate.now();
    response.setTodaysAttendance(
        attendanceRepository.countByCheckInTimeBetween(
            today.atStartOfDay(ZoneOffset.UTC).toInstant(),
            today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
    BigDecimal revenue = paymentRepository.sumPaidAmount();
    response.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
    response.setPendingPayments(paymentRepository.countByStatus(PaymentStatus.PENDING));
    return response;
  }
}
