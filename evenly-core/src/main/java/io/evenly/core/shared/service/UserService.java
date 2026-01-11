package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.User;

import java.util.Optional;

/**
 * Service interface for user operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface UserService {
    Optional<User> findById(String userId);
    User getOrCreate(String userId, String email, String username);
}
