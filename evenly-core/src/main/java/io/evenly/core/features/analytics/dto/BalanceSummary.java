package io.evenly.core.features.analytics.dto;

import java.math.BigDecimal;

/**
 * BalanceSummary DTO matching OpenAPI schema.
 */
public class BalanceSummary {
    private BigDecimal userTotalPaidAmount;
    private BigDecimal userTotalExpectedAmount;
    private BigDecimal workspaceTotalPaidAmount;
    private BigDecimal budgetLimit;
    private BigDecimal spentPercentage;
    private String currency;

    public BalanceSummary() {
    }

    // Getters and setters
    public BigDecimal getUserTotalPaidAmount() {
        return userTotalPaidAmount;
    }

    public void setUserTotalPaidAmount(BigDecimal userTotalPaidAmount) {
        this.userTotalPaidAmount = userTotalPaidAmount;
    }

    public BigDecimal getUserTotalExpectedAmount() {
        return userTotalExpectedAmount;
    }

    public void setUserTotalExpectedAmount(BigDecimal userTotalExpectedAmount) {
        this.userTotalExpectedAmount = userTotalExpectedAmount;
    }

    public BigDecimal getWorkspaceTotalPaidAmount() {
        return workspaceTotalPaidAmount;
    }

    public void setWorkspaceTotalPaidAmount(BigDecimal workspaceTotalPaidAmount) {
        this.workspaceTotalPaidAmount = workspaceTotalPaidAmount;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public BigDecimal getSpentPercentage() {
        return spentPercentage;
    }

    public void setSpentPercentage(BigDecimal spentPercentage) {
        this.spentPercentage = spentPercentage;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
