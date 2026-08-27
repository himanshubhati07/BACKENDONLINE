package com.example.app.service;

import com.example.app.document.User;
import com.example.app.dto.PageResponse;
import com.example.app.dto.UserCreateRequest;
import com.example.app.dto.UserResponse;
import com.example.app.dto.UserUpdateRequest;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Set<String> SORT_FIELDS = Set.of("name", "email", "createdAt", "updatedAt");
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

    public UserResponse create(UserCreateRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException("Email already registered");
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("USER"));
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User saved = repository.save(user);
        log.info("Created user id {}", saved.getId());
        return toResponse(saved);
    }

    public PageResponse<UserResponse> findAll(long offset, int limit, String sortBy, String direction, String filter) {
        if (offset < 0) throw new IllegalArgumentException("offset must be at least 0");
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("limit must be between 1 and 100");
        if (!SORT_FIELDS.contains(sortBy)) throw new IllegalArgumentException("Unsupported sort field");
        Sort.Direction sortDirection;
        try { sortDirection = Sort.Direction.fromString(direction); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("direction must be asc or desc"); }
        Query query = new Query();
        if (filter != null && !filter.isBlank()) {
            String value = Pattern.quote(filter.trim());
            query.addCriteria(new Criteria().orOperator(Criteria.where("name").regex(value, "i"),
                    Criteria.where("email").regex(value, "i")));
        }
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class);
        query.with(Sort.by(sortDirection, sortBy)).skip(offset).limit(limit);
        List<UserResponse> content = mongoTemplate.find(query, User.class).stream().map(UserService::toResponse).toList();
        return new PageResponse<>(content, offset, limit, total);
    }

    public UserResponse findById(String id) { return toResponse(get(id)); }

    public UserResponse update(String id, UserUpdateRequest request) {
        User user = get(id);
        String email = normalizeEmail(request.email());
        repository.findByEmailIgnoreCase(email).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new DuplicateResourceException("Email already registered"); });
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setUpdatedAt(Instant.now());
        User saved = repository.save(user);
        log.info("Updated user id {}", saved.getId());
        return toResponse(saved);
    }

    public void delete(String id) {
        User user = get(id);
        repository.delete(user);
        log.info("Deleted user id {}", id);
    }

    private User get(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is required");
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(); }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), Set.copyOf(user.getRoles()),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
