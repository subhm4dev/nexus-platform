package com.nexus.healthcare.notification.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private UUID id;
    private UUID userId;
    private String notificationType;
    private String title;
    private String message;
    private String channel;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}

