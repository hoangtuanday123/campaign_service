package com.example.userservice.repository;

import com.example.userservice.domain.document.UserActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityLogRepository extends MongoRepository<UserActivityLog, String> {
}
