package io.evenly.core.shared.dto.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UpdateExpenseRequest DTO matching OpenAPI schema.
 */
public class UpdateExpenseRequest {
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String paidByUserId;
    private String categoryId;
    private LocalDate date;
    private String note;
    private List<String> participantIds;

    public UpdateExpenseRequest() {
    }

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(String paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }
}
