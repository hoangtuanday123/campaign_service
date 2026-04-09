package com.example.userservice.service.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.enums.ActivityAction;
import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;
import com.example.userservice.dto.auth.AuthResponse;
import com.example.userservice.dto.auth.LoginRequest;
import com.example.userservice.dto.auth.RegisterRequest;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.InvalidCredentialsException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;
import com.example.userservice.security.UserPrincipal;
import com.example.userservice.service.ActivityLogService;
import com.example.userservice.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ActivityLogService activityLogService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            ActivityLogService activityLogService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.activityLogService = activityLogService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        Set<Role> roles = request.roles() == null || request.roles().isEmpty()
                ? Set.of(Role.ROLE_USER)
                : request.roles();

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(savedUser);
        String token = jwtService.generateToken(principal);
        activityLogService.log(savedUser.getId(), ActivityAction.REGISTER, Map.of("username", savedUser.getUsername()));

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMillis(), userMapper.toResponse(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = resolveUsername(request.usernameOrEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        activityLogService.log(user.getId(), ActivityAction.LOGIN, Map.of("usernameOrEmail", request.usernameOrEmail()));

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMillis(), userMapper.toResponse(user));
    }

    private String resolveUsername(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(User::getUsername)
                .orElse(usernameOrEmail);
    }
}
