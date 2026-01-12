package io.evenly.core.domain;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invite {
    private UUID id;
    private UUID workspaceId;
    private String code;
    private Integer maxUses;
    private Integer usesCount;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}
