package io.evenly.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing a shared expense group (workspace).
 * Maps to the 'workspaces' table in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {
    private UUID id;
    private String name;
    private String defaultSplitMode; // 'EQUAL' or 'WEIGHTED'
    private BigDecimal monthlySharedLimit;
    private Boolean isPersonal;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
