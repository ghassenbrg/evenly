package io.evenly.core.features.auth.impl;

import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.features.auth.UserService;
import io.evenly.core.features.currencies.SupportedCurrency;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<io.evenly.core.features.auth.dto.User> findById(String userId) { // userId is now username (String)
        return userRepository.findById(userId)
            .map(this::toDto);
    }

    @Override
    @Transactional
    public io.evenly.core.features.auth.dto.User getOrCreate(String userId, String email, String username) { // userId is now username (String)
        return userRepository.findById(userId)
            .map(this::toDto)
            .orElseGet(() -> {
                // Create new user
                io.evenly.core.domain.User newUser = io.evenly.core.domain.User.builder()
                    .id(userId) // userId is now username (String)
                    .email(email)
                    .username(username)
                    .displayName(username) // Default to username
                    .preferredCurrency(SupportedCurrency.USD) // Default currency
                    .locale("en-US") // Default locale
                    .timezone("UTC") // Default timezone
                    .createdAt(OffsetDateTime.now()) // Set creation timestamp
                    .build();
                
                newUser = userRepository.save(newUser);
                return toDto(newUser);
            });
    }

    private io.evenly.core.features.auth.dto.User toDto(io.evenly.core.domain.User domain) {
        io.evenly.core.features.auth.dto.User dto = new io.evenly.core.features.auth.dto.User();
        dto.setId(domain.getId()); // id is now String, no need to convert
        dto.setEmail(domain.getEmail());
        dto.setUsername(domain.getUsername());
        dto.setDisplayName(domain.getDisplayName());
        dto.setAvatarUrl(domain.getAvatarUrl());
        dto.setPreferredCurrency(domain.getPreferredCurrency() != null ? domain.getPreferredCurrency().getCode() : null);
        dto.setLocale(domain.getLocale() != null ? domain.getLocale() : "en-US");
        dto.setTimezone(domain.getTimezone() != null ? domain.getTimezone() : "UTC");
        dto.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : OffsetDateTime.now());
        return dto;
    }
}
