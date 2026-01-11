package io.evenly.core.features.auth;

import io.evenly.core.features.auth.dto.User;
import java.util.Optional;

/**
 * Service interface for user operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface UserService {
    Optional<User> findById(String userId);
    User getOrCreate(String userId, String email, String username);
}
