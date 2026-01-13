package io.evenly.core.domain;

import io.evenly.core.features.currencies.SupportedCurrency;
import jakarta.persistence.*;
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
@Entity
@Table(name = "workspaces")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "default_split_mode", nullable = false, length = 20)
    private String defaultSplitMode; // 'EQUAL' or 'WEIGHTED'
    
    @Column(name = "monthly_shared_limit", precision = 15, scale = 2)
    private BigDecimal monthlySharedLimit;
    
    @Column(name = "is_personal", nullable = false)
    @Builder.Default
    private Boolean isPersonal = false;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private SupportedCurrency currency;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
