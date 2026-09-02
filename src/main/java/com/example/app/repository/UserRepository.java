package com.example.app.repository;

import com.example.app.entity.User;
import com.example.app.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

  Page<User>
      findByDeletedFalseAndStatusAndEmailContainingIgnoreCaseOrDeletedFalseAndStatusAndFirstNameContainingIgnoreCaseOrDeletedFalseAndStatusAndLastNameContainingIgnoreCase(
          UserStatus status,
          String email,
          UserStatus repeatedStatusOne,
          String firstName,
          UserStatus repeatedStatusTwo,
          String lastName,
          Pageable pageable);

  Page<User> findByDeletedFalseAndStatus(UserStatus status, Pageable pageable);

  Page<User>
      findByDeletedFalseAndEmailContainingIgnoreCaseOrDeletedFalseAndFirstNameContainingIgnoreCaseOrDeletedFalseAndLastNameContainingIgnoreCase(
          String email, String firstName, String lastName, Pageable pageable);

  Page<User> findByDeletedFalse(Pageable pageable);
}
