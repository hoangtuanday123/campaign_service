package com.example.promotionengine.client.user;

import com.example.promotionengine.dto.user.UserSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface UserServiceClient {

    Optional<UserSnapshot> getUser(UUID userId);
}