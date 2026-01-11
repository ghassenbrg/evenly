package io.evenly.core.shared.dto;

import java.time.OffsetDateTime;

/**
 * Settlement DTO matching OpenAPI schema.
 */
public class Settlement {
    private String id;
    private String workspaceId;
    private String createdByUserId;
    private OffsetDateTime createdAt;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
