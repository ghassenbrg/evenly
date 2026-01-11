package io.evenly.core.features.balance.dto;

import java.util.List;

/**
 * SettleUpResponse DTO matching OpenAPI schema.
 */
public class SettleUpResponse {
    private String currency;
    private SettleUpMember currentUser;
    private List<SettleUpMember> otherMembers;

    public SettleUpResponse() {
    }

    public SettleUpResponse(String currency, SettleUpMember currentUser, List<SettleUpMember> otherMembers) {
        this.currency = currency;
        this.currentUser = currentUser;
        this.otherMembers = otherMembers;
    }

    // Getters and setters
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public SettleUpMember getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(SettleUpMember currentUser) {
        this.currentUser = currentUser;
    }

    public List<SettleUpMember> getOtherMembers() {
        return otherMembers;
    }

    public void setOtherMembers(List<SettleUpMember> otherMembers) {
        this.otherMembers = otherMembers;
    }
}
