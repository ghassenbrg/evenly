package io.evenly.core.features.settlements.dto;

import io.evenly.core.features.auth.dto.User;
import java.time.OffsetDateTime;

/**
 * Settlement DTO matching OpenAPI schema.
 */
public class Settlement {
    private String id;
    private String workspaceId;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private String note;
    private String createdByUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime revertedAt;
    private String revertedByUserId;
    private User createdBy;

    public Settlement() {
    }

    public Settlement(String id, String workspaceId, String createdByUserId) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.createdByUserId = createdByUserId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public java.time.LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(java.time.LocalDate startDate) {
        this.startDate = startDate;
    }

    public java.time.LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(java.time.LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getRevertedAt() {
        return revertedAt;
    }

    public void setRevertedAt(OffsetDateTime revertedAt) {
        this.revertedAt = revertedAt;
    }

    public String getRevertedByUserId() {
        return revertedByUserId;
    }

    public void setRevertedByUserId(String revertedByUserId) {
        this.revertedByUserId = revertedByUserId;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
