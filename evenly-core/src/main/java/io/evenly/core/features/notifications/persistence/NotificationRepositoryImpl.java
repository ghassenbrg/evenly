package io.evenly.core.features.notifications.persistence;

import io.evenly.core.domain.Notification;
import io.evenly.core.domain.NotificationEntityType;
import io.evenly.core.domain.NotificationType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of NotificationRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
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
    public List<Notification> findByRecipientUserId(String userId) {
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.recipientUserId = :userId ORDER BY n.createdAt DESC",
            Notification.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public List<Notification> findByRecipientUserIdAndRead(String userId, boolean read) {
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.recipientUserId = :userId AND n.isRead = :read ORDER BY n.createdAt DESC",
            Notification.class)
            .setParameter("userId", userId)
            .setParameter("read", read)
            .getResultList();
    }

    @Override
    public List<Notification> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.workspaceId = :workspaceId ORDER BY n.createdAt DESC",
            Notification.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID());
            if (notification.getCreatedAt() == null) {
                notification.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (notification.getIsRead() == null) {
                notification.setIsRead(false);
            }
            entityManager.persist(notification);
            return notification;
        } else {
            return entityManager.merge(notification);
        }
    }

    @Override
    public void delete(UUID id) {
        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            entityManager.remove(notification);
        }
    }

    @Override
    public void markAsRead(UUID id) {
        entityManager.createQuery("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markAllAsRead(String userId) {
        entityManager.createQuery("UPDATE Notification n SET n.isRead = true WHERE n.recipientUserId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public long countUnreadByUserId(String userId) {
        return entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.recipientUserId = :userId AND n.isRead = false", Long.class)
            .setParameter("userId", userId)
            .getSingleResult();
    }

    @Override
    public boolean existsByRecipientAndTypeAndEntity(String recipientUserId, NotificationType type, NotificationEntityType entityType, String entityId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.recipientUserId = :recipientUserId AND n.type = :type AND n.entityType = :entityType AND n.entityId = :entityId",
            Long.class)
            .setParameter("recipientUserId", recipientUserId)
            .setParameter("type", type)
            .setParameter("entityType", entityType)
            .setParameter("entityId", entityId)
            .getSingleResult();
        return count > 0;
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
