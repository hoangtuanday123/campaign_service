package com.example.campaignservice.security;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET = "c3VwZXItc2VjdXJlLXN1cGVyLXNlY3VyZS1zdXBlci1zZWNyZS1rZXktZm9yLXVzZXItc2VydmljZQ==";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000));

    @Test
    void shouldExtractAndValidateUserFromToken() {
        UUID userId = UUID.randomUUID();
        String token = Jwts.builder()
                .claims(Map.of("userId", userId.toString()))
                .subject("alice")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        AuthenticatedUser principal = jwtService.extractAuthenticatedUser(token);

        assertThat(principal.getId()).isEqualTo(userId);
        assertThat(principal.getUsername()).isEqualTo("alice");
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }
}