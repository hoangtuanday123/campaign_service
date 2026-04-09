package com.example.userservice.service;

import com.example.userservice.domain.enums.ActivityAction;

import java.util.Map;
import java.util.UUID;

public interface ActivityLogService {

    void log(UUID userId, ActivityAction action, Map<String, Object> metadata);
}
