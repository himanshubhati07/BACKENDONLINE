package com.example.app.repository;

import com.example.app.entity.Member;
import com.example.app.entity.Subscription;
import com.example.app.entity.SubscriptionStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  Page<Subscription> findByMemberId(Long memberId, Pageable pageable);

  Optional<Subscription> findFirstByMemberAndStatusOrderByEndDateDesc(
      Member member, SubscriptionStatus status);
}
