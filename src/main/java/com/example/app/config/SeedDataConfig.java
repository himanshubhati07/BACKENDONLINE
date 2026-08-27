package com.example.app.config;

import com.example.app.document.User;
import com.example.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Configuration
public class SeedDataConfig {
    @Bean
    CommandLineRunner seedData(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!repository.existsByEmailIgnoreCase("seed@example.com")) {
                User user = new User();
                user.setName("Seed User");
                user.setEmail("seed@example.com");
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRoles(Set.of("USER"));
                Instant now = Instant.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                repository.save(user);
            }
        };
    }
}
