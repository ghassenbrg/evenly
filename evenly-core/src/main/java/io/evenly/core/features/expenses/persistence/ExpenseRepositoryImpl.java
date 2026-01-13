package io.evenly.core.features.expenses.persistence;

import io.evenly.core.domain.Expense;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of ExpenseRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class ExpenseRepositoryImpl implements io.evenly.core.domain.repository.ExpenseRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Expense> findById(UUID id) {
        Expense expense = entityManager.find(Expense.class, id);
        return Optional.ofNullable(expense);
    }

    @Override
    public List<Expense> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT e FROM Expense e WHERE e.workspaceId = :workspaceId ORDER BY e.effectiveDate DESC", 
            Expense.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public List<Expense> findByWorkspaceId(UUID workspaceId, LocalDate startDate, LocalDate endDate,
                                           UUID categoryId, int page, int size, String sort) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM Expense e WHERE e.workspaceId = :workspaceId");
        
        if (startDate != null) {
            jpql.append(" AND e.effectiveDate >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND e.effectiveDate <= :endDate");
        }
        if (categoryId != null) {
            jpql.append(" AND e.categoryId = :categoryId");
        }
        
        // Default sort
        String orderBy = " ORDER BY e.effectiveDate DESC";
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String direction = parts[1].trim().toUpperCase();
                orderBy = " ORDER BY e." + field + " " + direction;
            }
        }
        jpql.append(orderBy);
        
        TypedQuery<Expense> query = entityManager.createQuery(jpql.toString(), Expense.class)
            .setParameter("workspaceId", workspaceId);
        
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        
        return query.setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }

    @Override
    public List<Expense> findByPaidByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT e FROM Expense e WHERE e.paidByUserId = :userId ORDER BY e.effectiveDate DESC", 
            Expense.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public List<Expense> findByCategoryId(UUID categoryId) {
        return entityManager.createQuery(
            "SELECT e FROM Expense e WHERE e.categoryId = :categoryId", 
            Expense.class)
            .setParameter("categoryId", categoryId)
            .getResultList();
    }

    @Override
    public Expense save(Expense expense) {
        if (expense.getId() == null) {
            expense.setId(UUID.randomUUID());
            if (expense.getCreatedAt() == null) {
                expense.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (expense.getUpdatedAt() == null) {
                expense.setUpdatedAt(java.time.OffsetDateTime.now());
            }
            entityManager.persist(expense);
            return expense;
        } else {
            expense.setUpdatedAt(java.time.OffsetDateTime.now());
            return entityManager.merge(expense);
        }
    }

    @Override
    public void delete(UUID id) {
        Expense expense = entityManager.find(Expense.class, id);
        if (expense != null) {
            entityManager.remove(expense);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(e) FROM Expense e WHERE e.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public long countByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT COUNT(e) FROM Expense e WHERE e.workspaceId = :workspaceId", Long.class)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult();
    }

    @Override
    public long countByWorkspaceIdAndDateRange(UUID workspaceId, LocalDate startDate, LocalDate endDate) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM Expense e WHERE e.workspaceId = :workspaceId");
        
        if (startDate != null) {
            jpql.append(" AND e.effectiveDate >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND e.effectiveDate <= :endDate");
        }
        
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class)
            .setParameter("workspaceId", workspaceId);
        
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        
        return query.getSingleResult();
    }
}
