package io.evenly.core.features.analytics.dto;

/**
 * LinearChartDataPoint DTO matching OpenAPI schema.
 */
public class LinearChartDataPoint {
    private String date;
    private java.math.BigDecimal amount;

    public LinearChartDataPoint() {
    }

    public LinearChartDataPoint(String date, java.math.BigDecimal amount) {
        this.date = date;
        this.amount = amount;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }
}
