package io.evenly.core.shared.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * UpdatePaymentRequest DTO matching OpenAPI schema.
 */
public class UpdatePaymentRequest {
    private String payeeUserId;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String note;
    private LocalDate effectiveDate;
    
    @Pattern(regexp = "COMPLETED|PENDING|FAILED", message = "Status must be COMPLETED, PENDING, or FAILED")
    private String status;

    public UpdatePaymentRequest() {
    }

    // Getters and setters
    public String getPayeeUserId() {
        return payeeUserId;
    }

    public void setPayeeUserId(String payeeUserId) {
        this.payeeUserId = payeeUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
