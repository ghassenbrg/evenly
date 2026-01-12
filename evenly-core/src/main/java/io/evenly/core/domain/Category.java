package io.evenly.core.domain;

import jakarta.persistence.*;
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
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "workspace_id", columnDefinition = "UUID")
    private UUID workspaceId; // null for global categories
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "slug", nullable = false, length = 255)
    private String slug;
    
    @Column(name = "icon", nullable = false, length = 100)
    private String icon;
    
    @Column(name = "color", nullable = false, length = 7)
    private String color;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
