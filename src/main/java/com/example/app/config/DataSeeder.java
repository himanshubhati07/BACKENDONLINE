package com.example.app.config;

import com.example.app.document.Product;
import com.example.app.document.Role;
import com.example.app.document.User;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String SEED_ADMIN_EMAIL = "admin@example.com";
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final int GENERATED_PASSWORD_LENGTH = 16;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedProducts();
    }

    /**
     * Seeds a development-only admin account with a freshly generated random password.
     * The password is never hardcoded and is only ever printed once to the startup log.
     * This never runs when a "prod"/"production" profile is active.
     */
    private void seedAdminUser() {
        if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
            log.info("Production profile detected - skipping development admin seeding.");
            return;
        }

        if (userRepository.existsByEmail(SEED_ADMIN_EMAIL)) {
            return;
        }

        String generatedPassword = generateRandomPassword();
        Instant now = Instant.now();
        User admin = User.builder()
                .name("Admin User")
                .email(SEED_ADMIN_EMAIL)
                .password(passwordEncoder.encode(generatedPassword))
                .role(Role.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userRepository.save(admin);

        log.warn("Seeded a development admin account (email={}). Generated one-time password: {} "
                        + "-- this password is random per startup, is not stored anywhere else, and will not be shown again.",
                SEED_ADMIN_EMAIL, generatedPassword);
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        String allChars = UPPER + LOWER + DIGITS + SPECIAL;

        List<Character> passwordChars = new ArrayList<>(GENERATED_PASSWORD_LENGTH);
        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        for (int i = passwordChars.size(); i < GENERATED_PASSWORD_LENGTH; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }
        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        passwordChars.forEach(password::append);
        return password.toString();
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
