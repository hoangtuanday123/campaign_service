package com.example.userservice.service.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.enums.ActivityAction;
import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;
import com.example.userservice.dto.user.UpdateUserRequest;
import com.example.userservice.dto.user.UserResponse;
import com.example.userservice.exception.UnauthorizedOperationException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ActivityLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private User actor;
    private User target;

    @BeforeEach
    void setUp() {
        actor = User.builder()
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

        target = User.builder()
                .id(actor.getId())
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .phoneNumber("+84123456789")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_USER))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(actor.getUsername(), null, java.util.List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateShouldPersistChangesForOwnProfile() {
        UpdateUserRequest request = new UpdateUserRequest("alice-new", "alice.new@example.com", "+84987654321");
        UserResponse response = new UserResponse(
                target.getId(), "alice-new", "alice.new@example.com", "+84987654321",
                target.getStatus(), target.getRoles(), target.getCreatedAt(), target.getUpdatedAt()
        );

        when(userRepository.findByUsername(actor.getUsername())).thenReturn(Optional.of(actor));
        when(userRepository.findById(actor.getId())).thenReturn(Optional.of(target));
        when(userRepository.existsByUsernameAndIdNot("alice-new", actor.getId())).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("alice.new@example.com", actor.getId())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(response);

        UserResponse updated = userService.update(actor.getId(), request);

        assertThat(updated.username()).isEqualTo("alice-new");
        verify(activityLogService).log(eq(actor.getId()), eq(ActivityAction.UPDATE_PROFILE), any());
    }

    @Test
    void softDeleteShouldMarkUserInactive() {
        when(userRepository.findByUsername(actor.getUsername())).thenReturn(Optional.of(actor));
        when(userRepository.findById(actor.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.softDelete(actor.getId());

        assertThat(target.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(activityLogService).log(eq(actor.getId()), eq(ActivityAction.SOFT_DELETE), any());
    }

    @Test
    void updateShouldRejectOtherUsersForNonAdmin() {
        User other = User.builder()
                .id(UUID.randomUUID())
                .username("bob")
                .email("bob@example.com")
                .password("encoded")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_USER))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByUsername(actor.getUsername())).thenReturn(Optional.of(actor));
        when(userRepository.findById(other.getId())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(other.getId(), new UpdateUserRequest("bob2", null, null)))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessage("You are not allowed to modify this user");
    }
}
