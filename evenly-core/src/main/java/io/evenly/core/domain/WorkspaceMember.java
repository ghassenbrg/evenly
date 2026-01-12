package io.evenly.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing the relationship between a user and a workspace.
 * Maps to the 'workspace_members' table in the database.
 * This is a junction entity for the many-to-many relationship between users and workspaces.
 */
@Entity
@Table(name = "workspace_members")
@IdClass(WorkspaceMemberId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMember {
    @Id
    @Column(name = "workspace_id", columnDefinition = "UUID")
    private UUID workspaceId;
    
    @Id
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // Changed to String (username)
    
    @Column(name = "role", nullable = false, length = 20)
    private String role; // 'OWNER' or 'MEMBER'
    
    @Column(name = "weight_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weightPercent = new BigDecimal("100.00");
    
    @Column(name = "personal_monthly_limit", precision = 15, scale = 2)
    private BigDecimal personalMonthlyLimit;
    
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;
}
