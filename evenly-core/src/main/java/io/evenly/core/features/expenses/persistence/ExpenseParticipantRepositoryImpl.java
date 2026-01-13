package io.evenly.core.features.expenses.persistence;

import io.evenly.core.domain.ExpenseParticipant;
import io.evenly.core.domain.ExpenseParticipantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * JPA-based implementation of ExpenseParticipantRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class ExpenseParticipantRepositoryImpl implements io.evenly.core.domain.repository.ExpenseParticipantRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public List<ExpenseParticipant> findByExpenseId(UUID expenseId) {
        return entityManager.createQuery(
            "SELECT ep FROM ExpenseParticipant ep WHERE ep.expenseId = :expenseId", 
            ExpenseParticipant.class)
            .setParameter("expenseId", expenseId)
            .getResultList();
    }

    @Override
    public List<ExpenseParticipant> findByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT ep FROM ExpenseParticipant ep WHERE ep.userId = :userId", 
            ExpenseParticipant.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public void save(ExpenseParticipant participant) {
        entityManager.merge(participant);
    }

    @Override
    public void saveAll(List<ExpenseParticipant> participants) {
        for (ExpenseParticipant participant : participants) {
            entityManager.merge(participant);
        }
        entityManager.flush();
    }

    @Override
    public void deleteByExpenseId(UUID expenseId) {
        entityManager.createQuery("DELETE FROM ExpenseParticipant ep WHERE ep.expenseId = :expenseId")
            .setParameter("expenseId", expenseId)
            .executeUpdate();
    }

    @Override
    public void delete(UUID expenseId, String userId) { // userId is now String (username)
        ExpenseParticipantId id = new ExpenseParticipantId(expenseId, userId);
        ExpenseParticipant participant = entityManager.find(ExpenseParticipant.class, id);
        if (participant != null) {
            entityManager.remove(participant);
        }
    }

    @Override
    public boolean existsByExpenseIdAndUserId(UUID expenseId, String userId) { // userId is now String (username)
        Long count = entityManager.createQuery(
            "SELECT COUNT(ep) FROM ExpenseParticipant ep WHERE ep.expenseId = :expenseId AND ep.userId = :userId", 
            Long.class)
            .setParameter("expenseId", expenseId)
            .setParameter("userId", userId)
            .getSingleResult();
        return count > 0;
    }
}
