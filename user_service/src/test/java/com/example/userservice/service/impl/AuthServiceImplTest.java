package com.example.userservice.service.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.enums.ActivityAction;
import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;
import com.example.userservice.dto.auth.AuthResponse;
import com.example.userservice.dto.auth.LoginRequest;
import com.example.userservice.dto.auth.RegisterRequest;
import com.example.userservice.dto.user.UserResponse;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.InvalidCredentialsException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;
import com.example.userservice.service.ActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .phoneNumber("+84123456789")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_USER))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void registerShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "Password123", "+84123456789", null);
        UserResponse userResponse = new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhoneNumber(),
                user.getStatus(), user.getRoles(), user.getCreatedAt(), user.getUpdatedAt()
        );

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMillis()).thenReturn(3600000L);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().username()).isEqualTo("alice");
        verify(activityLogService).log(eq(user.getId()), eq(ActivityAction.REGISTER), any());
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "Password123", null, null);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username is already in use");
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("alice", "Password123");
        UserResponse userResponse = new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhoneNumber(),
                user.getStatus(), user.getRoles(), user.getCreatedAt(), user.getUpdatedAt()
        );

        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMillis()).thenReturn(3600000L);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(activityLogService).log(eq(user.getId()), eq(ActivityAction.LOGIN), any());
    }

    @Test
    void loginShouldThrowWhenAuthenticationFails() {
        LoginRequest request = new LoginRequest("alice", "bad");

        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username/email or password");
    }
}
