package com.example.app.repository;

import com.example.app.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

  boolean existsByEmail(String email);
}
