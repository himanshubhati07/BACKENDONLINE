package com.example.app.service;

import com.example.app.dto.TrainerRequest;
import com.example.app.entity.Trainer;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerService {

  @Autowired private TrainerRepository trainerRepository;

  public Page<Trainer> list(Pageable pageable) {
    return trainerRepository.findAll(pageable);
  }

  public Trainer get(Long id) {
    return trainerRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + id));
  }

  @Transactional
  public Trainer create(TrainerRequest request) {
    if (trainerRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Trainer email already exists");
    }
    Trainer trainer = new Trainer();
    trainer.setName(request.getName());
    trainer.setEmail(request.getEmail());
    trainer.setPhone(request.getPhone());
    trainer.setSpecialization(request.getSpecialization());
    return trainerRepository.save(trainer);
  }

  @Transactional
  public Trainer update(Long id, TrainerRequest request) {
    Trainer trainer = get(id);
    trainer.setName(request.getName());
    trainer.setEmail(request.getEmail());
    trainer.setPhone(request.getPhone());
    trainer.setSpecialization(request.getSpecialization());
    return trainerRepository.save(trainer);
  }

  @Transactional
  public void delete(Long id) {
    Trainer trainer = get(id);
    trainerRepository.delete(trainer);
  }
}
