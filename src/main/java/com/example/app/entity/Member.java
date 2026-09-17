package com.example.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "members",
    indexes = {
      @jakarta.persistence.Index(name = "idx_members_email", columnList = "email"),
      @jakarta.persistence.Index(name = "idx_members_phone", columnList = "phone")
    })
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(length = 20)
  private String phone;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trainer_id")
  private Trainer trainer;

  @Column(nullable = false)
  private LocalDate membershipStartDate;

  @Column private LocalDate membershipExpiryDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Trainer getTrainer() {
    return trainer;
  }

  public void setTrainer(Trainer trainer) {
    this.trainer = trainer;
  }

  public LocalDate getMembershipStartDate() {
    return membershipStartDate;
  }

  public void setMembershipStartDate(LocalDate membershipStartDate) {
    this.membershipStartDate = membershipStartDate;
  }

  public LocalDate getMembershipExpiryDate() {
    return membershipExpiryDate;
  }

  public void setMembershipExpiryDate(LocalDate membershipExpiryDate) {
    this.membershipExpiryDate = membershipExpiryDate;
  }

  public MembershipStatus getStatus() {
    return status;
  }

  public void setStatus(MembershipStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
