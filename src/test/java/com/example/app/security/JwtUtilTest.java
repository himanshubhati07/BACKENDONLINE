package com.example.app.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private final User details = (User) User.withUsername("test@example.com").password("hash").authorities("ROLE_USER").build();

    @Test
    void generatesAndValidatesHs256Token() {
        JwtUtil util = new JwtUtil("a-very-long-test-secret-with-at-least-32-characters", 1_800_000);
        String token = util.generateToken(details);
        assertEquals("test@example.com", util.extractUsername(token));
        assertTrue(util.isTokenValid(token, details));
        assertEquals(1800, util.getExpirationSeconds());
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtUtil util = new JwtUtil("a-very-long-test-secret-with-at-least-32-characters", 1);
        String token = util.generateToken(details);
        Thread.sleep(10);
        assertThrows(ExpiredJwtException.class, () -> util.extractUsername(token));
    }

    @Test
    void shortOrBlankSecretNeverBreaksConstruction() {
        assertDoesNotThrow(() -> new JwtUtil("short", 0));
        assertDoesNotThrow(() -> new JwtUtil("", -1));
    }
}
