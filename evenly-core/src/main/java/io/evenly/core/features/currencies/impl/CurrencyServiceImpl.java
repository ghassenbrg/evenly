package io.evenly.core.features.currencies.impl;

import io.evenly.core.features.currencies.CurrencyService;
import io.evenly.core.features.currencies.SupportedCurrency;
import io.evenly.core.features.currencies.dto.Currency;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CurrencyServiceImpl implements CurrencyService {

    @Override
    public List<Currency> findAll() {
        return Arrays.stream(SupportedCurrency.values())
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private Currency toDto(SupportedCurrency currency) {
        Currency dto = new Currency();
        dto.setCode(currency.getCode());
        dto.setName(currency.getName());
        dto.setSymbol(currency.getSymbol());
        return dto;
    }
}
