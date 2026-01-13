package io.evenly.core.features.workspaces.dto;

import java.time.OffsetDateTime;

/**
 * Workspace DTO matching OpenAPI schema.
 */
public class Workspace {
    private String id;
    private String name;
    private String defaultSplitMode; // EQUAL, WEIGHTED
    private Double monthlySharedLimit;
    private Boolean isPersonal;
    private String currency;
    private String inviteCode;
    private String inviteLink;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Workspace() {
    }

    public Workspace(String id, String name, String defaultSplitMode, String currency) {
        this.id = id;
        this.name = name;
        this.defaultSplitMode = defaultSplitMode;
        this.currency = currency;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultSplitMode() {
        return defaultSplitMode;
    }

    public void setDefaultSplitMode(String defaultSplitMode) {
        this.defaultSplitMode = defaultSplitMode;
    }

    public Double getMonthlySharedLimit() {
        return monthlySharedLimit;
    }

    public void setMonthlySharedLimit(Double monthlySharedLimit) {
        this.monthlySharedLimit = monthlySharedLimit;
    }

    public Boolean getIsPersonal() {
        return isPersonal;
    }

    public void setIsPersonal(Boolean isPersonal) {
        this.isPersonal = isPersonal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }
}
