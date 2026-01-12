package io.evenly.core.domain;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private UUID id;
    private UUID workspaceId;
    private UUID categoryId; // nullable for uncategorized expenses
    private BigDecimal amount;
    private String currency;
    private LocalDate effectiveDate;
    private String note;
    private UUID paidByUserId;
    private UUID createdByUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
