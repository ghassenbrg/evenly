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
 * Domain entity representing a direct payment between users.
 * Maps to the 'payments' table in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private UUID id;
    private UUID workspaceId;
    private UUID payeeUserId;
    private UUID paidByUserId;
    private BigDecimal amount;
    private String currency;
    private LocalDate effectiveDate;
    private String note;
    private String status; // 'COMPLETED', 'PENDING', or 'FAILED'
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
