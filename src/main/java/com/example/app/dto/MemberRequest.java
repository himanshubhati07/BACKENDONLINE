package com.example.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class MemberRequest {

  @NotBlank private String name;

  @NotBlank @Email private String email;

  private String phone;

  private Long trainerId;

  private LocalDate membershipStartDate;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Long getTrainerId() {
    return trainerId;
  }

  public void setTrainerId(Long trainerId) {
    this.trainerId = trainerId;
  }

  public LocalDate getMembershipStartDate() {
    return membershipStartDate;
  }

  public void setMembershipStartDate(LocalDate membershipStartDate) {
    this.membershipStartDate = membershipStartDate;
  }
}
