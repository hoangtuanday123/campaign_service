package com.example.userservice.service.impl;

import com.example.userservice.domain.document.UserActivityLog;
import com.example.userservice.domain.enums.ActivityAction;
import com.example.userservice.repository.UserActivityLogRepository;
import com.example.userservice.service.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);

    private final UserActivityLogRepository activityLogRepository;

    public ActivityLogServiceImpl(UserActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    public void log(UUID userId, ActivityAction action, Map<String, Object> metadata) {
        UserActivityLog activityLog = UserActivityLog.builder()
                .userId(userId)
                .action(action)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();
        try {
            activityLogRepository.save(activityLog);
        } catch (Exception ex) {
            // Activity logging should not break core user flows if MongoDB is temporarily unavailable.
            log.warn("Failed to persist activity log for userId={} action={}", userId, action, ex);
        }
    }
}
