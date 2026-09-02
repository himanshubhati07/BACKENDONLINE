package com.example.app.service;

import com.example.app.dto.PageResponse;
import com.example.app.dto.UserRequest;
import com.example.app.dto.UserResponse;
import com.example.app.entity.User;
import com.example.app.entity.UserStatus;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse create(UserRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateResourceException("Email already exists");
    }
    User user = new User();
    apply(request, user);
    User saved = userRepository.save(user);
    LOGGER.info("Created user id={}", saved.getId());
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public UserResponse get(Long id) {
    return toResponse(findActive(id));
  }

  @Transactional(readOnly = true)
  public PageResponse<UserResponse> list(
      int offset, int limit, String sort, UserStatus status, String search) {
    if (offset < 0 || limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          "Offset must be non-negative and limit must be between 1 and 100");
    }
    String[] sortParts = sort.split(",", 2);
    String property =
        switch (sortParts[0]) {
          case "id", "firstName", "lastName", "email", "status", "createdAt", "updatedAt" ->
              sortParts[0];
          default -> "createdAt";
        };
    Sort.Direction direction =
        sortParts.length == 2 && "asc".equalsIgnoreCase(sortParts[1])
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(direction, property));
    Page<User> page;
    boolean hasSearch = search != null && !search.isBlank();
    if (status != null && hasSearch) {
      page =
          userRepository
              .findByDeletedFalseAndStatusAndEmailContainingIgnoreCaseOrDeletedFalseAndStatusAndFirstNameContainingIgnoreCaseOrDeletedFalseAndStatusAndLastNameContainingIgnoreCase(
                  status, search, status, search, status, search, pageable);
    } else if (status != null) {
      page = userRepository.findByDeletedFalseAndStatus(status, pageable);
    } else if (hasSearch) {
      page =
          userRepository
              .findByDeletedFalseAndEmailContainingIgnoreCaseOrDeletedFalseAndFirstNameContainingIgnoreCaseOrDeletedFalseAndLastNameContainingIgnoreCase(
                  search, search, search, pageable);
    } else {
      page = userRepository.findByDeletedFalse(pageable);
    }
    return new PageResponse<>(
        page.map(this::toResponse).getContent(), page.getTotalElements(), offset, limit);
  }

  public UserResponse update(Long id, UserRequest request) {
    User user = findActive(id);
    if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
      throw new DuplicateResourceException("Email already exists");
    }
    apply(request, user);
    User saved = userRepository.save(user);
    LOGGER.info("Updated user id={}", saved.getId());
    return toResponse(saved);
  }

  public void delete(Long id) {
    User user = findActive(id);
    user.setDeleted(true);
    userRepository.save(user);
    LOGGER.info("Soft deleted user id={}", id);
  }

  private User findActive(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.isDeleted()) {
      throw new ResourceNotFoundException("User not found");
    }
    return user;
  }

  private void apply(UserRequest request, User user) {
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setEmail(request.email().trim().toLowerCase());
    user.setPhone(request.phone().trim());
    user.setStatus(request.status() == null ? UserStatus.ACTIVE : request.status());
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPhone(),
        user.getStatus(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
