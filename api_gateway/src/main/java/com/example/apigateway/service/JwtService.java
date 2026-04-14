package com.example.apigateway.service;

import com.example.apigateway.config.JwtProperties;
import com.example.apigateway.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public AuthenticatedUser validate(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidJwtException("Authorization token is required");
        }

        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration == null || !expiration.after(new Date())) {
                throw new InvalidJwtException("JWT token has expired");
            }

            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);
            if (username == null || username.isBlank() || userId == null || userId.isBlank()) {
                throw new InvalidJwtException("JWT token is invalid");
            }

            return new AuthenticatedUser(UUID.fromString(userId), username);
        } catch (IllegalArgumentException ex) {
            throw new InvalidJwtException("JWT token is invalid", ex);
        } catch (JwtException ex) {
            throw new InvalidJwtException("JWT token is invalid", ex);
        }
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

    public record AuthenticatedUser(UUID userId, String username) {
    }
}
