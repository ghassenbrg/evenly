package io.evenly.core.features.currencies;

import io.evenly.core.features.currencies.dto.Currency;

import java.util.List;

/**
 * Service interface for currency operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface CurrencyService {
    List<Currency> findAll();
}
