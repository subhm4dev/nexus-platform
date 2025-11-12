package com.nexus.healthcare.notification.service;

import com.nexus.healthcare.notification.model.request.NotificationRequest;
import com.nexus.healthcare.notification.model.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    NotificationResponse sendNotification(UUID tenantId, UUID domainId, NotificationRequest request);
    
    List<NotificationResponse> getNotificationsByUser(UUID userId, UUID tenantId, UUID domainId);
    
    void sendAppointmentReminder(UUID appointmentId, UUID tenantId, UUID domainId);
    
    void sendPaymentLink(UUID appointmentId, String paymentLink, UUID tenantId, UUID domainId);
    
    void sendRefundNotification(UUID userId, UUID tenantId, UUID domainId, java.math.BigDecimal refundAmount);
}

