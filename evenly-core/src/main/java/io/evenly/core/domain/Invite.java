package io.evenly.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing a workspace invitation code.
 * Maps to the 'invites' table in the database.
 */
@Entity
@Table(name = "invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invite {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "workspace_id", nullable = false, columnDefinition = "UUID")
    private UUID workspaceId;
    
    @Column(name = "code", nullable = false, unique = true, length = 255)
    private String code;
    
    @Column(name = "max_uses")
    private Integer maxUses;
    
    @Column(name = "uses_count", nullable = false)
    @Builder.Default
    private Integer usesCount = 0;
    
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
