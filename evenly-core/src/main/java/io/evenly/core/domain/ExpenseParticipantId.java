package io.evenly.core.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for ExpenseParticipant entity.
 */
public class ExpenseParticipantId implements Serializable {
    private UUID expenseId;
    private String userId; // Changed to String (username)

    public ExpenseParticipantId() {
    }

    public ExpenseParticipantId(UUID expenseId, String userId) {
        this.expenseId = expenseId;
        this.userId = userId;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(UUID expenseId) {
        this.expenseId = expenseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseParticipantId that = (ExpenseParticipantId) o;
        return Objects.equals(expenseId, that.expenseId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, userId);
    }
}
