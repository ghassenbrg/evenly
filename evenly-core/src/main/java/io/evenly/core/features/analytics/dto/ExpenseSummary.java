package io.evenly.core.features.analytics.dto;

import java.util.List;

/**
 * ExpenseSummary DTO matching OpenAPI schema.
 */
public class ExpenseSummary {
    private java.math.BigDecimal totalAmount;
    private Integer expensesCount;
    private java.math.BigDecimal averagePerDay;
    private String currency;
    private java.math.BigDecimal largestExpenseAmount;
    private List<LinearChartDataPoint> linearChartData;

    public ExpenseSummary() {
    }

    // Getters and setters
    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getExpensesCount() {
        return expensesCount;
    }

    public void setExpensesCount(Integer expensesCount) {
        this.expensesCount = expensesCount;
    }

    public java.math.BigDecimal getAveragePerDay() {
        return averagePerDay;
    }

    public void setAveragePerDay(java.math.BigDecimal averagePerDay) {
        this.averagePerDay = averagePerDay;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public java.math.BigDecimal getLargestExpenseAmount() {
        return largestExpenseAmount;
    }

    public void setLargestExpenseAmount(java.math.BigDecimal largestExpenseAmount) {
        this.largestExpenseAmount = largestExpenseAmount;
    }

    public List<LinearChartDataPoint> getLinearChartData() {
        return linearChartData;
    }

    public void setLinearChartData(List<LinearChartDataPoint> linearChartData) {
        this.linearChartData = linearChartData;
    }
}
