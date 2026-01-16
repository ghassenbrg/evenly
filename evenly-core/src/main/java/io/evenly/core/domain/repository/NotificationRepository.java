package io.evenly.core.domain.repository;

import io.evenly.core.domain.Notification;
import io.evenly.core.domain.NotificationEntityType;
import io.evenly.core.domain.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Notification domain entities.
 * Port in the ports & adapters architecture.
 */
public interface NotificationRepository {
    Optional<Notification> findById(UUID id);
    List<Notification> findByRecipientUserId(String userId);
    List<Notification> findByRecipientUserIdAndRead(String userId, boolean read);
    List<Notification> findByWorkspaceId(UUID workspaceId);
    Notification save(Notification notification);
    void delete(UUID id);
    void markAsRead(UUID id);
    void markAllAsRead(String userId);
    long countUnreadByUserId(String userId);
    boolean existsByRecipientAndTypeAndEntity(String recipientUserId, NotificationType type, NotificationEntityType entityType, String entityId);
    boolean existsById(UUID id);
}
