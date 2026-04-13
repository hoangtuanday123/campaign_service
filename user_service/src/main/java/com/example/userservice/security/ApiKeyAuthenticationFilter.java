package com.example.userservice.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyAuthenticationFilter(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(apiKeyProperties.header());
        
        if (apiKey != null && !apiKey.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (!apiKey.equals(apiKeyProperties.value())) {
                    throw new BadCredentialsException("Invalid API Key");
                }

                // Create authentication for valid API key
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "api-key-user",
                        apiKey,
                        Collections.emptyList()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BadCredentialsException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
