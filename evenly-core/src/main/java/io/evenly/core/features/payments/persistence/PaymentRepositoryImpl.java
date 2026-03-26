package io.evenly.core.features.payments.persistence;

import io.evenly.core.domain.Payment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.evenly.core.shared.common.SettlementScope;

/**
 * JPA-based implementation of PaymentRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class PaymentRepositoryImpl implements io.evenly.core.domain.repository.PaymentRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Payment> findById(UUID id) {
        Payment payment = entityManager.find(Payment.class, id);
        return Optional.ofNullable(payment);
    }

    @Override
    public List<Payment> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT p FROM Payment p WHERE p.workspaceId = :workspaceId ORDER BY p.effectiveDate DESC, p.createdAt DESC, p.id DESC",
            Payment.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public List<Payment> findByWorkspaceId(UUID workspaceId, LocalDate startDate, LocalDate endDate,
                                           String status, SettlementScope settlementScope, int page, int size, String sort) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Payment p WHERE p.workspaceId = :workspaceId");
        
        if (startDate != null) {
            jpql.append(" AND p.effectiveDate >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND p.effectiveDate <= :endDate");
        }
        if (status != null) {
            jpql.append(" AND p.status = :status");
        }
        appendSettlementFilter(jpql, settlementScope, "p");
        
        jpql.append(buildOrderByClause("p", sort));
        
        TypedQuery<Payment> query = entityManager.createQuery(jpql.toString(), Payment.class)
            .setParameter("workspaceId", workspaceId);
        
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (status != null) {
            query.setParameter("status", status);
        }
        
        return query.setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }

    @Override
    public List<Payment> findByPayeeUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT p FROM Payment p WHERE p.payeeUserId = :userId ORDER BY p.effectiveDate DESC, p.createdAt DESC, p.id DESC",
            Payment.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public List<Payment> findByPaidByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT p FROM Payment p WHERE p.paidByUserId = :userId ORDER BY p.effectiveDate DESC, p.createdAt DESC, p.id DESC",
            Payment.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public List<Payment> findBySettlementId(UUID settlementId) {
        return entityManager.createQuery(
            "SELECT p FROM Payment p WHERE p.settlementId = :settlementId", Payment.class)
            .setParameter("settlementId", settlementId)
            .getResultList();
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(UUID.randomUUID());
            if (payment.getCreatedAt() == null) {
                payment.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (payment.getUpdatedAt() == null) {
                payment.setUpdatedAt(java.time.OffsetDateTime.now());
            }
            entityManager.persist(payment);
            return payment;
        } else {
            payment.setUpdatedAt(java.time.OffsetDateTime.now());
            return entityManager.merge(payment);
        }
    }

    @Override
    public void delete(UUID id) {
        Payment payment = entityManager.find(Payment.class, id);
        if (payment != null) {
            entityManager.remove(payment);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(p) FROM Payment p WHERE p.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public long countByWorkspaceId(UUID workspaceId, SettlementScope settlementScope) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Payment p WHERE p.workspaceId = :workspaceId");
        appendSettlementFilter(jpql, settlementScope, "p");
        return entityManager.createQuery(jpql.toString(), Long.class)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult();
    }

    private void appendSettlementFilter(StringBuilder jpql, SettlementScope settlementScope, String alias) {
        if (settlementScope == null || settlementScope == SettlementScope.ALL) {
            return;
        }
        if (settlementScope == SettlementScope.UNSETTLED) {
            jpql.append(" AND ").append(alias).append(".settlementId IS NULL");
        } else if (settlementScope == SettlementScope.SETTLED) {
            jpql.append(" AND ").append(alias).append(".settlementId IS NOT NULL");
        }
    }

    private String buildOrderByClause(String alias, String sort) {
        String primaryField = "effectiveDate";
        String direction = "DESC";

        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                primaryField = parts[0].trim();
                direction = parts[1].trim().toUpperCase();
            }
        }

        StringBuilder orderBy = new StringBuilder(" ORDER BY ")
            .append(alias).append('.').append(primaryField).append(' ').append(direction);

        if (!"createdAt".equals(primaryField)) {
            orderBy.append(", ").append(alias).append(".createdAt ").append(direction);
        }
        if (!"id".equals(primaryField)) {
            orderBy.append(", ").append(alias).append(".id ").append(direction);
        }

        return orderBy.toString();
    }
}
