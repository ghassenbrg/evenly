package io.evenly.core.domain;

import jakarta.persistence.*;
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
@Entity
@Table(name = "expense_participants")
@IdClass(ExpenseParticipantId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipant {
    @Id
    @Column(name = "expense_id", columnDefinition = "UUID")
    private UUID expenseId;
    
    @Id
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // Changed to String (username)
}
