package com.example.userservice.mapper;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;
import com.example.userservice.dto.user.UserResponse;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-10T10:40:43+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String username = null;
        String email = null;
        String phoneNumber = null;
        UserStatus status = null;
        Set<Role> roles = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        status = user.getStatus();
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<Role>( set );
        }
        createdAt = user.getCreatedAt();
        updatedAt = user.getUpdatedAt();

        UserResponse userResponse = new UserResponse( id, username, email, phoneNumber, status, roles, createdAt, updatedAt );

        return userResponse;
    }
}
