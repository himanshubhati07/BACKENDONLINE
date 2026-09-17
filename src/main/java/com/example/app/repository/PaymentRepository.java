package com.example.app.repository;

import com.example.app.entity.Payment;
import com.example.app.entity.PaymentStatus;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Page<Payment> findByMemberId(Long memberId, Pageable pageable);

  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

  long countByStatus(PaymentStatus status);

  @Query(
      "select coalesce(sum(p.amount), 0) from Payment p where p.status ="
          + " com.example.app.entity.PaymentStatus.PAID")
  BigDecimal sumPaidAmount();
}
