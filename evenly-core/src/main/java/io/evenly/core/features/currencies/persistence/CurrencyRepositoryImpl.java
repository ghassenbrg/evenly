package io.evenly.core.features.currencies.persistence;

import io.evenly.core.domain.Currency;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * JPA-based implementation of CurrencyRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class CurrencyRepositoryImpl implements io.evenly.core.domain.repository.CurrencyRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Currency> findByCode(String code) {
        return entityManager.createQuery(
            "SELECT c FROM Currency c WHERE c.code = :code", Currency.class)
            .setParameter("code", code)
            .getResultStream()
            .findFirst();
    }

    @Override
    public List<Currency> findAll() {
        return entityManager.createQuery(
            "SELECT c FROM Currency c ORDER BY c.code", Currency.class)
            .getResultList();
    }

    @Override
    public boolean existsByCode(String code) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM Currency c WHERE c.code = :code", Long.class)
            .setParameter("code", code)
            .getSingleResult();
        return count > 0;
    }
}
