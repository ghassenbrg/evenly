package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Notification;

import java.util.List;

/**
 * Service interface for notification operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface NotificationService {
    List<Notification> findAllForUser(String userId);
    Integer getUnreadCount(String userId);
    void markAsRead(String notificationId);
    void markAllAsRead(String userId);
}
