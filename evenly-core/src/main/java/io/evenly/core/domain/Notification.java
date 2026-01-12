package io.evenly.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing a user notification.
 * Maps to the 'notifications' table in the database.
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // Changed to String (username)
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "workspace_id", columnDefinition = "UUID")
    private UUID workspaceId; // nullable for non-workspace notifications
    
    @Column(name = "read", nullable = false)
    @Builder.Default
    private Boolean read = false;
    
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;
    
    @Column(name = "context", nullable = false, length = 50)
    @Builder.Default
    private String context = "GENERAL"; // 'PAYMENT', 'EXPENSE', 'WORKSPACE', 'GENERAL'
}
