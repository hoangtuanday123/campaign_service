package com.example.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.expirationMillis());

        return Jwts.builder()
                .claims(Map.of("userId", principal.getId().toString()))
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).get("userId", String.class));
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        Claims claims = extractClaims(token);
        return principal.getUsername().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    public long getExpirationMillis() {
        return jwtProperties.expirationMillis();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
