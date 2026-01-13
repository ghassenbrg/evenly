package io.evenly.core.features.auth.persistence;

import io.evenly.core.domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Optional;

/**
 * JPA-based implementation of UserRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class UserRepositoryImpl implements io.evenly.core.domain.repository.UserRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<User> findById(String id) { // id is now username (String)
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class)
            .setParameter("email", email)
            .getResultStream()
            .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = :username", User.class)
            .setParameter("username", username)
            .getResultStream()
            .findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // Use username as id if id is not set
            if (user.getUsername() != null) {
                user.setId(user.getUsername());
            } else {
                throw new IllegalArgumentException("User must have a username to set as id");
            }
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(java.time.OffsetDateTime.now());
            }
            entityManager.persist(user);
            return user;
        } else {
            return entityManager.merge(user);
        }
    }

    @Override
    public boolean existsById(String id) { // id is now username (String)
        return entityManager.createQuery(
            "SELECT COUNT(u) > 0 FROM User u WHERE u.id = :id", Boolean.class)
            .setParameter("id", id)
            .getSingleResult();
    }

    @Override
    public boolean existsByEmail(String email) {
        return entityManager.createQuery(
            "SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email", Boolean.class)
            .setParameter("email", email)
            .getSingleResult();
    }

    @Override
    public boolean existsByUsername(String username) {
        return entityManager.createQuery(
            "SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username", Boolean.class)
            .setParameter("username", username)
            .getSingleResult();
    }
}
