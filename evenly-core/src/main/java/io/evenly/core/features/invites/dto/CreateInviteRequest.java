package io.evenly.core.features.invites.dto;

import jakarta.validation.constraints.Min;

/**
 * CreateInviteRequest DTO matching OpenAPI schema.
 */
public class CreateInviteRequest {
    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;
    
    private Integer expiresInDays;

    public CreateInviteRequest() {
    }

    // Getters and setters
    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getExpiresInDays() {
        return expiresInDays;
    }

    public void setExpiresInDays(Integer expiresInDays) {
        this.expiresInDays = expiresInDays;
    }
}
