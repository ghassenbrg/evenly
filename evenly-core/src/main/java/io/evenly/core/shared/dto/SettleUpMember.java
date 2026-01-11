package io.evenly.core.shared.dto;

import java.math.BigDecimal;

/**
 * SettleUpMember DTO matching OpenAPI schema.
 */
public class SettleUpMember {
    private String userId;
    private String userFullName;
    private BigDecimal paidAmount;
    private BigDecimal expectedAmount;

    public SettleUpMember() {
    }

    public SettleUpMember(String userId, String userFullName, BigDecimal paidAmount, BigDecimal expectedAmount) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.paidAmount = paidAmount;
        this.expectedAmount = expectedAmount;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }
}
