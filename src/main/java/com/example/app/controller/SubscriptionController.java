package com.example.app.controller;

import com.example.app.dto.SubscriptionRequest;
import com.example.app.entity.Subscription;
import com.example.app.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Member subscription management")
public class SubscriptionController {

  @Autowired private SubscriptionService subscriptionService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Assign a subscription to a member (admin only)")
  public ResponseEntity<Subscription> assign(@Valid @RequestBody SubscriptionRequest request) {
    return ResponseEntity.ok(subscriptionService.assign(request));
  }

  @PutMapping("/member/{memberId}/renew")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Renew a member's subscription (admin only)")
  public ResponseEntity<Subscription> renew(@PathVariable Long memberId) {
    return ResponseEntity.ok(subscriptionService.renew(memberId));
  }

  @PutMapping("/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Cancel a subscription (admin only)")
  public ResponseEntity<Subscription> cancel(@PathVariable Long id) {
    return ResponseEntity.ok(subscriptionService.cancel(id));
  }

  @GetMapping("/member/{memberId}/current")
  @Operation(summary = "Get a member's current active subscription")
  public ResponseEntity<Subscription> current(@PathVariable Long memberId) {
    return ResponseEntity.ok(subscriptionService.current(memberId));
  }

  @GetMapping("/member/{memberId}/history")
  @Operation(summary = "Get a member's subscription history (paginated)")
  public ResponseEntity<Page<Subscription>> history(
      @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(subscriptionService.history(memberId, pageable));
  }
}
