package com.example.app.config;

import com.example.app.document.Product;
import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@example.com")) {
            Instant now = Instant.now();
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(admin);
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            Instant now = Instant.now();
            productRepository.saveAll(java.util.List.of(
                    Product.builder()
                            .name("Wireless Mouse")
                            .description("Ergonomic wireless mouse with USB receiver")
                            .price(new BigDecimal("19.99"))
                            .category("electronics")
                            .quantity(150)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    Product.builder()
                            .name("Mechanical Keyboard")
                            .description("RGB backlit mechanical keyboard")
                            .price(new BigDecimal("59.99"))
                            .category("electronics")
                            .quantity(80)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    Product.builder()
                            .name("Office Chair")
                            .description("Adjustable ergonomic office chair")
                            .price(new BigDecimal("149.50"))
                            .category("furniture")
                            .quantity(30)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }
    }
}
