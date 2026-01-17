package io.evenly.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing a settlement record.
 * Maps to the 'settlements' table in the database.
 */
@Entity
@Table(name = "settlements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "workspace_id", nullable = false, columnDefinition = "UUID")
    private UUID workspaceId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "reverted_at")
    private OffsetDateTime revertedAt;

    @Column(name = "reverted_by_user_id", length = 100)
    private String revertedByUserId;
}
