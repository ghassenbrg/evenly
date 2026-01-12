package io.evenly.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain entity representing the relationship between an expense and a participating user.
 * Maps to the 'expense_participants' table in the database.
 * This is a junction entity for the many-to-many relationship between expenses and users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipant {
    private UUID expenseId;
    private UUID userId;
}
