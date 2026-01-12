package io.evenly.core.features.notifications.persistence;

import io.evenly.core.domain.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of NotificationRepository.
 */
@ApplicationScoped
public class NotificationRepositoryImpl implements io.evenly.core.domain.repository.NotificationRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Notification> findById(UUID id) {
        Notification notification = entityManager.find(Notification.class, id);
        return Optional.ofNullable(notification);
    }

    @Override
    public List<Notification> findByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.timestamp DESC", 
            Notification.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public List<Notification> findByUserIdAndRead(String userId, boolean read) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId AND n.read = :read ORDER BY n.timestamp DESC", 
            Notification.class)
            .setParameter("userId", userId)
            .setParameter("read", read)
            .getResultList();
    }

    @Override
    public List<Notification> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.workspaceId = :workspaceId ORDER BY n.timestamp DESC", 
            Notification.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID());
            if (notification.getTimestamp() == null) {
                notification.setTimestamp(java.time.OffsetDateTime.now());
            }
            if (notification.getRead() == null) {
                notification.setRead(false);
            }
            entityManager.persist(notification);
            return notification;
        } else {
            return entityManager.merge(notification);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            entityManager.remove(notification);
        }
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        entityManager.createQuery("UPDATE Notification n SET n.read = true WHERE n.id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) { // userId is now String (username)
        entityManager.createQuery("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public long countUnreadByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false", Long.class)
            .setParameter("userId", userId)
            .getSingleResult();
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        return count > 0;
    }
}
