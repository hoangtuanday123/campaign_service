package com.example.userservice.mapper;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
