package io.evenly.core.mock.repository;

import io.evenly.core.domain.User;
import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.mock.data.MockDataProvider;
import io.evenly.core.mock.config.MockProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Mock implementation of UserRepository.
 * Only active when mock profile is enabled.
 */
@Alternative
@MockProfile
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class MockUserRepository implements UserRepository {

    @Inject
    private MockDataProvider mockDataProvider;

    @Override
    public Optional<User> findById(String id) { // id is now username (String)
        return Optional.ofNullable(mockDataProvider.getUsers().get(id))
            .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mockDataProvider.getUsers().values().stream()
            .filter(u -> email.equals(u.getEmail()))
            .findFirst()
            .map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mockDataProvider.getUsers().values().stream()
            .filter(u -> username.equals(u.getUsername()))
            .findFirst()
            .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // Use username as id if id is not set
            if (user.getUsername() != null) {
                user = User.builder()
                    .id(user.getUsername()) // id is now username (String)
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .preferredCurrency(user.getPreferredCurrency())
                    .locale(user.getLocale())
                    .timezone(user.getTimezone())
                    .createdAt(user.getCreatedAt())
                    .build();
            } else {
                throw new IllegalArgumentException("User must have a username to set as id");
            }
        }
        // In mock, we'd typically store in a map, but MockDataProvider uses DTOs
        // For now, just return the user
        return user;
    }

    @Override
    public boolean existsById(String id) { // id is now username (String)
        return mockDataProvider.getUsers().containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return mockDataProvider.getUsers().values().stream()
            .anyMatch(u -> email.equals(u.getEmail()));
    }

    @Override
    public boolean existsByUsername(String username) {
        return mockDataProvider.getUsers().values().stream()
            .anyMatch(u -> username.equals(u.getUsername()));
    }

    private User toDomain(io.evenly.core.features.auth.dto.User dto) {
        return User.builder()
            .id(dto.getId()) // id is now String, no need to convert
            .email(dto.getEmail())
            .displayName(dto.getDisplayName())
            .username(dto.getUsername())
            .avatarUrl(dto.getAvatarUrl())
            .preferredCurrency(dto.getPreferredCurrency())
            .locale(dto.getLocale())
            .timezone(dto.getTimezone())
            .createdAt(dto.getCreatedAt())
            .build();
    }
}
