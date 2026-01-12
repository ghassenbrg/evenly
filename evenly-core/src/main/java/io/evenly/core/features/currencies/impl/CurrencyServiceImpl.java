package io.evenly.core.features.currencies.impl;

import io.evenly.core.domain.repository.CurrencyRepository;
import io.evenly.core.features.currencies.CurrencyService;
import io.evenly.core.features.currencies.dto.Currency;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CurrencyServiceImpl implements CurrencyService {

    @Inject
    private CurrencyRepository currencyRepository;

    @Override
    public List<io.evenly.core.features.currencies.dto.Currency> findAll() {
        return currencyRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private io.evenly.core.features.currencies.dto.Currency toDto(io.evenly.core.domain.Currency domain) {
        io.evenly.core.features.currencies.dto.Currency dto = new io.evenly.core.features.currencies.dto.Currency();
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setSymbol(domain.getSymbol());
        return dto;
    }
}
