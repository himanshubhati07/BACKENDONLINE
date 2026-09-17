package com.example.app.controller;

import com.example.app.dto.PaymentRequest;
import com.example.app.entity.Payment;
import com.example.app.entity.PaymentStatus;
import com.example.app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment recording and search")
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @GetMapping
  @Operation(summary = "List/search payments (paginated), optionally filter by member or status")
  public ResponseEntity<Page<Payment>> list(
      @RequestParam(required = false) Long memberId,
      @RequestParam(required = false) String status,
      @PageableDefault(size = 20) Pageable pageable) {
    if (memberId != null) {
      return ResponseEntity.ok(paymentService.byMember(memberId, pageable));
    }
    if (status != null && !status.isBlank()) {
      return ResponseEntity.ok(
          paymentService.byStatus(PaymentStatus.valueOf(status.toUpperCase()), pageable));
    }
    return ResponseEntity.ok(paymentService.list(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a payment by id")
  public ResponseEntity<Payment> get(@PathVariable Long id) {
    return ResponseEntity.ok(paymentService.get(id));
  }

  @PostMapping
  @Operation(summary = "Record a payment")
  public ResponseEntity<Payment> record(@Valid @RequestBody PaymentRequest request) {
    return ResponseEntity.ok(paymentService.record(request));
  }
}
