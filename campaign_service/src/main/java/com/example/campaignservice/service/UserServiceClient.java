package com.example.campaignservice.service;

import com.example.campaignservice.integration.user.UserSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface UserServiceClient {

    Optional<UserSnapshot> getUser(UUID userId);
}