package io.evenly.core.domain.repository;

import io.evenly.core.domain.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Currency domain entities (reference data).
 * Port in the ports & adapters architecture.
 */
public interface CurrencyRepository {
    Optional<Currency> findByCode(String code);
    List<Currency> findAll();
    boolean existsByCode(String code);
}
