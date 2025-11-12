package com.nexus.ecommerce.fulfilment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Delivery Preference Entity
 * Stores customer delivery preferences and scheduling
 */
@Entity
@Table(name = "delivery_preferences", indexes = {
    @Index(name = "idx_preference_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_preference_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_preference_customer", columnList = "customer_user_id"),
    @Index(name = "idx_preference_tenant", columnList = "tenant_id"),
    @Index(name = "idx_preference_scheduled_date", columnList = "scheduled_delivery_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_id", nullable = false)
    private Fulfillment fulfillment;
    
    @Column(name = "fulfillment_id", insertable = false, updatable = false)
    private UUID fulfillmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    
    @Column(name = "delivery_id", insertable = false, updatable = false)
    private UUID deliveryId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "customer_user_id", nullable = false)
    private UUID customerUserId;
    
    // Scheduling
    @Column(name = "scheduled_delivery_date")
    private LocalDate scheduledDeliveryDate;
    
    @Column(name = "scheduled_delivery_time_start")
    private LocalTime scheduledDeliveryTimeStart;
    
    @Column(name = "scheduled_delivery_time_end")
    private LocalTime scheduledDeliveryTimeEnd;
    
    @Column(name = "delivery_time_window", length = 100)
    private String deliveryTimeWindow;
    // "MORNING", "AFTERNOON", "EVENING", "9AM-12PM", etc.
    
    // Delivery instructions
    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;
    
    @Column(name = "special_handling_notes", columnDefinition = "TEXT")
    private String specialHandlingNotes;
    
    @Column(name = "leave_at_door")
    @Builder.Default
    private Boolean leaveAtDoor = false;
    
    @Column(name = "hand_to_customer")
    @Builder.Default
    private Boolean handToCustomer = true;
    
    @Column(name = "require_signature")
    @Builder.Default
    private Boolean requireSignature = false;
    
    // Contact preferences
    @Column(name = "preferred_contact_method", length = 50)
    private String preferredContactMethod;
    // PHONE, SMS, EMAIL, APP
    
    @Column(name = "preferred_contact_time", length = 100)
    private String preferredContactTime;
    
    @Column(name = "do_not_disturb")
    @Builder.Default
    private Boolean doNotDisturb = false;
    
    // Location preferences
    @Column(name = "preferred_delivery_location", length = 200)
    private String preferredDeliveryLocation;
    // "Front door", "Back gate", etc.
    
    @Column(name = "gate_code", length = 50)
    private String gateCode;
    
    @Column(name = "building_name", length = 200)
    private String buildingName;
    
    @Column(name = "floor_number", length = 20)
    private String floorNumber;
    
    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;
    
    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

