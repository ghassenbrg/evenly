package io.evenly.core.shared.dto;

/**
 * WorkspaceMember DTO matching OpenAPI schema.
 */
public class WorkspaceMember {
    private String userId;
    private String role; // OWNER, MEMBER
    private Double weightPercent;
    private Double personalMonthlyLimit;
    private User user;

    public WorkspaceMember() {
    }

    public WorkspaceMember(String userId, String role, User user) {
        this.userId = userId;
        this.role = role;
        this.user = user;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(Double weightPercent) {
        this.weightPercent = weightPercent;
    }

    public Double getPersonalMonthlyLimit() {
        return personalMonthlyLimit;
    }

    public void setPersonalMonthlyLimit(Double personalMonthlyLimit) {
        this.personalMonthlyLimit = personalMonthlyLimit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
