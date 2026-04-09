package com.example.userservice.domain.document;

import com.example.userservice.domain.enums.ActivityAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_activity_logs")
public class UserActivityLog {

    @Id
    private String id;

    private UUID userId;

    private ActivityAction action;

    private Map<String, Object> metadata;

    private Instant timestamp;
}
