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
 * Domain entity representing a direct payment between users.
 * Maps to the 'payments' table in the database.
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "workspace_id", nullable = false, columnDefinition = "UUID")
    private UUID workspaceId;
    
    @Column(name = "payee_user_id", nullable = false, length = 100)
    private String payeeUserId; // Changed to String (username)
    
    @Column(name = "paid_by_user_id", nullable = false, length = 100)
    private String paidByUserId; // Changed to String (username)
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private SupportedCurrency currency;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "settlement_id", columnDefinition = "UUID")
    private UUID settlementId;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 'COMPLETED', 'PENDING', or 'FAILED'
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
