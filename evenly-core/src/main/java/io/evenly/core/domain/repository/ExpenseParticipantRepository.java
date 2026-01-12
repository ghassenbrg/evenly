package io.evenly.core.domain.repository;

import io.evenly.core.domain.ExpenseParticipant;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ExpenseParticipant domain entities.
 * Port in the ports & adapters architecture.
 */
public interface ExpenseParticipantRepository {
    List<ExpenseParticipant> findByExpenseId(UUID expenseId);
    List<ExpenseParticipant> findByUserId(String userId); // userId is now String (username)
    void save(ExpenseParticipant participant);
    void saveAll(List<ExpenseParticipant> participants);
    void deleteByExpenseId(UUID expenseId);
    void delete(UUID expenseId, String userId); // userId is now String (username)
    boolean existsByExpenseIdAndUserId(UUID expenseId, String userId); // userId is now String (username)
}
