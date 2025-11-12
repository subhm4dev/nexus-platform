package com.nexus.healthcare.notification.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Notification type is required")
    private String notificationType;
    
    @NotNull(message = "Title is required")
    private String title;
    
    @NotNull(message = "Message is required")
    private String message;
    
    @NotNull(message = "Channel is required")
    private String channel; // SMS, EMAIL, PUSH, WHATSAPP
    
    private Map<String, Object> metadata;
}

