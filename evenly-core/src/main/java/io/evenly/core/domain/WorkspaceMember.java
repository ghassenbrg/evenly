package io.evenly.core.domain;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMember {
    private UUID workspaceId;
    private UUID userId;
    private String role; // 'OWNER' or 'MEMBER'
    private BigDecimal weightPercent;
    private BigDecimal personalMonthlyLimit;
    private OffsetDateTime joinedAt;
}
