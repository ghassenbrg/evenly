package io.evenly.core.mock.service;

import io.evenly.core.features.currencies.CurrencyService;
import io.evenly.core.features.currencies.dto.Currency;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Mock implementation of CurrencyService.
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class CurrencyServiceMock implements CurrencyService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Currency> findAll() {
        return mockDataProvider.getCurrencies();
    }
}
