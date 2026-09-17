package com.example.app.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

  @Size(max = 100)
  private String name;

  private String phone;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
