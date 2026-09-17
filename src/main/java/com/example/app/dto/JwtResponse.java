package com.example.app.dto;

public class JwtResponse {

  private String token;

  private String refreshToken;

  private String tokenType = "Bearer";

  private Long userId;

  private String email;

  private String role;

  public JwtResponse() {}

  public JwtResponse(String token, String refreshToken, Long userId, String email, String role) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
