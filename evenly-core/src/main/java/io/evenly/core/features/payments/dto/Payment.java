package io.evenly.core.features.payments.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payment DTO matching OpenAPI schema.
 */
public class Payment {
    private String id;
    private String payeeUserId;
    private String payeeUserName;
    private BigDecimal amount;
    private String currency;
    private LocalDate effectiveDate;
    private String note;
    private String status; // COMPLETED, PENDING, FAILED
    private String paidByUserId;
    private String paidByUserName;

    public Payment() {
    }

    public Payment(String id, String payeeUserId, BigDecimal amount, String currency) {
        this.id = id;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayeeUserId() {
        return payeeUserId;
    }

    public void setPayeeUserId(String payeeUserId) {
        this.payeeUserId = payeeUserId;
    }

    public String getPayeeUserName() {
        return payeeUserName;
    }

    public void setPayeeUserName(String payeeUserName) {
        this.payeeUserName = payeeUserName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(String paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public String getPaidByUserName() {
        return paidByUserName;
    }

    public void setPaidByUserName(String paidByUserName) {
        this.paidByUserName = paidByUserName;
    }
}
