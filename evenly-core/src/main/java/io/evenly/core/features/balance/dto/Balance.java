package io.evenly.core.features.balance.dto;

import io.evenly.core.features.auth.dto.User;
import java.math.BigDecimal;

/**
 * Balance DTO matching OpenAPI schema.
 */
public class Balance {
    private String userId;
    private BigDecimal paid;
    private BigDecimal expected;
    private BigDecimal balance;
    private User user;

    public Balance() {
    }

    public Balance(String userId, BigDecimal paid, BigDecimal expected, User user) {
        this.userId = userId;
        this.paid = paid;
        this.expected = expected;
        this.balance = paid.subtract(expected);
        this.user = user;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public void setExpected(BigDecimal expected) {
        this.expected = expected;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
