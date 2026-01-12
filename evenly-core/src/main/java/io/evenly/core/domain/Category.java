package io.evenly.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing an expense category.
 * Maps to the 'categories' table in the database.
 * Categories can be workspace-specific (workspaceId != null) or global (workspaceId == null).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private UUID id;
    private UUID workspaceId; // null for global categories
    private String name;
    private String slug;
    private String icon;
    private String color;
    private Boolean isActive;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
