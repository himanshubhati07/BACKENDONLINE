package com.example.app.service;

import com.example.app.dto.PaymentRequest;
import com.example.app.entity.Member;
import com.example.app.entity.Payment;
import com.example.app.entity.PaymentStatus;
import com.example.app.entity.Subscription;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.kafka.GymEventProducer;
import com.example.app.repository.MemberRepository;
import com.example.app.repository.PaymentRepository;
import com.example.app.repository.SubscriptionRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private SubscriptionRepository subscriptionRepository;

  @Autowired private GymEventProducer eventProducer;

  public Page<Payment> list(Pageable pageable) {
    return paymentRepository.findAll(pageable);
  }

  public Page<Payment> byMember(Long memberId, Pageable pageable) {
    return paymentRepository.findByMemberId(memberId, pageable);
  }

  public Page<Payment> byStatus(PaymentStatus status, Pageable pageable) {
    return paymentRepository.findByStatus(status, pageable);
  }

  public Payment get(Long id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
  }

  @Transactional
  public Payment record(PaymentRequest request) {
    Member member =
        memberRepository
            .findById(request.getMemberId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));
    Payment payment = new Payment();
    payment.setMember(member);
    if (request.getSubscriptionId() != null) {
      Subscription subscription =
          subscriptionRepository
              .findById(request.getSubscriptionId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Subscription not found: " + request.getSubscriptionId()));
      payment.setSubscription(subscription);
    }
    payment.setAmount(request.getAmount());
    PaymentStatus status = PaymentStatus.PENDING;
    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      try {
        status = PaymentStatus.valueOf(request.getStatus().toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Invalid payment status: " + request.getStatus());
      }
    }
    payment.setStatus(status);
    payment.setPaymentReference(
        "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    Payment saved = paymentRepository.save(payment);
    eventProducer.publish(
        "PAYMENT_RECORDED",
        "{\"paymentId\":"
            + saved.getId()
            + ",\"memberId\":"
            + member.getId()
            + ",\"status\":\""
            + saved.getStatus()
            + "\"}");
    return saved;
  }
}
