package com.example.app.dto;

import jakarta.validation.constraints.NotNull;

public class SubscriptionRequest {

  @NotNull private Long memberId;

  @NotNull private Long planId;

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public Long getPlanId() {
    return planId;
  }

  public void setPlanId(Long planId) {
    this.planId = planId;
  }
}
