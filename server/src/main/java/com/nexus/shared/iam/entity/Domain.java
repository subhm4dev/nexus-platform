package com.nexus.shared.iam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Entity
 * 
 * <p>Represents a business domain in the platform (ecommerce, hospital, food-delivery, etc.).
 * Each domain is a separate business vertical with its own data isolation.
 */
@Entity
@Table(name = "domains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Domain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique human-readable code: "ecommerce", "hospital", "food-delivery", etc.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Display name: "E-commerce", "Hospital Management", etc.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Optional description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the domain is enabled (can be disabled without deleting)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

