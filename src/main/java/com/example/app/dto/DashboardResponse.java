package com.example.app.dto;

import java.math.BigDecimal;

public class DashboardResponse {

  private long totalMembers;

  private long activeMembers;

  private long expiredMemberships;

  private long totalTrainers;

  private long todaysAttendance;

  private BigDecimal totalRevenue;

  private long pendingPayments;

  public long getTotalMembers() {
    return totalMembers;
  }

  public void setTotalMembers(long totalMembers) {
    this.totalMembers = totalMembers;
  }

  public long getActiveMembers() {
    return activeMembers;
  }

  public void setActiveMembers(long activeMembers) {
    this.activeMembers = activeMembers;
  }

  public long getExpiredMemberships() {
    return expiredMemberships;
  }

  public void setExpiredMemberships(long expiredMemberships) {
    this.expiredMemberships = expiredMemberships;
  }

  public long getTotalTrainers() {
    return totalTrainers;
  }

  public void setTotalTrainers(long totalTrainers) {
    this.totalTrainers = totalTrainers;
  }

  public long getTodaysAttendance() {
    return todaysAttendance;
  }

  public void setTodaysAttendance(long todaysAttendance) {
    this.todaysAttendance = todaysAttendance;
  }

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal totalRevenue) {
    this.totalRevenue = totalRevenue;
  }

  public long getPendingPayments() {
    return pendingPayments;
  }

  public void setPendingPayments(long pendingPayments) {
    this.pendingPayments = pendingPayments;
  }
}
