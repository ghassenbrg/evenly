package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Currency;

import java.util.List;

/**
 * Service interface for currency operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface CurrencyService {
    List<Currency> findAll();
}
