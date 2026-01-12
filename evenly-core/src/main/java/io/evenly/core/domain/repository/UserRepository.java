package io.evenly.core.domain.repository;

import io.evenly.core.domain.User;

import java.util.Optional;

/**
 * Repository interface for User domain entities.
 * Port in the ports & adapters architecture.
 */
public interface UserRepository {
    Optional<User> findById(String id); // id is now username (String)
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
    boolean existsById(String id); // id is now username (String)
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
