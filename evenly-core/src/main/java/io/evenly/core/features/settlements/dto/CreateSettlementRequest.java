package io.evenly.core.features.settlements.dto;

/**
 * CreateSettlementRequest DTO matching OpenAPI schema.
 */
public class CreateSettlementRequest {
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private java.util.List<String> expenseIds;
    private java.util.List<String> paymentIds;
    private String note;

    public CreateSettlementRequest() {
    }

    // Getters and setters
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public java.time.LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(java.time.LocalDate startDate) {
        this.startDate = startDate;
    }

    public java.time.LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(java.time.LocalDate endDate) {
        this.endDate = endDate;
    }

    public java.util.List<String> getExpenseIds() {
        return expenseIds;
    }

    public void setExpenseIds(java.util.List<String> expenseIds) {
        this.expenseIds = expenseIds;
    }

    public java.util.List<String> getPaymentIds() {
        return paymentIds;
    }

    public void setPaymentIds(java.util.List<String> paymentIds) {
        this.paymentIds = paymentIds;
    }
}
