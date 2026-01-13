package io.evenly.core.domain;

import io.evenly.core.features.currencies.SupportedCurrency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing an expense record.
 * Maps to the 'expenses' table in the database.
 */
@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "workspace_id", nullable = false, columnDefinition = "UUID")
    private UUID workspaceId;
    
    @Column(name = "category_id", columnDefinition = "UUID")
    private UUID categoryId; // nullable for uncategorized expenses
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private SupportedCurrency currency;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "paid_by_user_id", nullable = false, length = 100)
    private String paidByUserId; // Changed to String (username)
    
    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId; // Changed to String (username)
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
