package io.evenly.core.features.currencies;

import io.evenly.core.shared.common.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import io.evenly.core.features.currencies.dto.Currency;

/**
 * Mock implementation of CurrencyService.
 */
@ApplicationScoped
public class CurrencyServiceMock implements CurrencyService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Currency> findAll() {
        return mockDataProvider.getCurrencies();
    }
}
