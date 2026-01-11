package io.evenly.core.shared.dto;

import java.time.OffsetDateTime;

/**
 * Invite DTO matching OpenAPI schema.
 */
public class Invite {
    private String id;
    private String workspaceId;
    private String code;
    private Integer maxUses;
    private Integer usesCount;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public Invite() {
    }

    public Invite(String id, String workspaceId, String code) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getUsesCount() {
        return usesCount;
    }

    public void setUsesCount(Integer usesCount) {
        this.usesCount = usesCount;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
