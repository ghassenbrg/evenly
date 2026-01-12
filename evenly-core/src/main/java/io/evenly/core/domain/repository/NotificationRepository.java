package io.evenly.core.domain.repository;

import io.evenly.core.domain.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Notification domain entities.
 * Port in the ports & adapters architecture.
 */
public interface NotificationRepository {
    Optional<Notification> findById(UUID id);
    List<Notification> findByUserId(String userId); // userId is now String (username)
    List<Notification> findByUserIdAndRead(String userId, boolean read); // userId is now String (username)
    List<Notification> findByWorkspaceId(UUID workspaceId);
    Notification save(Notification notification);
    void delete(UUID id);
    void markAsRead(UUID id);
    void markAllAsRead(String userId); // userId is now String (username)
    long countUnreadByUserId(String userId); // userId is now String (username)
    boolean existsById(UUID id);
}
