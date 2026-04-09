package com.example.userservice.service.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.enums.ActivityAction;
import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;
import com.example.userservice.dto.user.UpdateUserRequest;
import com.example.userservice.dto.user.UserResponse;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.exception.UnauthorizedOperationException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ActivityLogService;
import com.example.userservice.service.UserService;
import com.example.userservice.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ActivityLogService activityLogService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return getById(SecurityUtils.currentUserId());
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User currentActor = getCurrentActor();
        User targetUser = findUser(id);
        ensureAllowed(currentActor, targetUser);

        if (request.username() != null && userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("Username is already in use");
        }
        if (request.email() != null && userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email is already in use");
        }

        if (request.username() != null) {
            targetUser.setUsername(request.username());
        }
        if (request.email() != null) {
            targetUser.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            targetUser.setPhoneNumber(request.phoneNumber());
        }

        User updatedUser = userRepository.save(targetUser);
        activityLogService.log(updatedUser.getId(), ActivityAction.UPDATE_PROFILE, Map.of("updatedBy", currentActor.getId().toString()));
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        User currentActor = getCurrentActor();
        User targetUser = findUser(id);
        ensureAllowed(currentActor, targetUser);

        targetUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(targetUser);
        activityLogService.log(targetUser.getId(), ActivityAction.SOFT_DELETE, Map.of("updatedBy", currentActor.getId().toString()));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void ensureAllowed(User actor, User target) {
        boolean isAdmin = actor.getRoles().contains(Role.ROLE_ADMIN);
        boolean sameUser = actor.getId().equals(target.getId());
        if (!isAdmin && !sameUser) {
            throw new UnauthorizedOperationException("You are not allowed to modify this user");
        }
    }
}
