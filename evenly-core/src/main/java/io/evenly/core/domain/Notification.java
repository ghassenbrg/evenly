package io.evenly.core.domain;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private UUID id;
    private UUID userId;
    private String type;
    private String content;
    private UUID workspaceId; // nullable for non-workspace notifications
    private Boolean read;
    private OffsetDateTime timestamp;
    private String context; // 'PAYMENT', 'EXPENSE', 'WORKSPACE', 'GENERAL'
}
