package io.evenly.core.features.notifications;

import io.evenly.core.shared.common.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import io.evenly.core.features.notifications.dto.Notification;

/**
 * Mock implementation of NotificationService.
 */
@ApplicationScoped
public class NotificationServiceMock implements NotificationService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Notification> findAllForUser(String userId) {
        return mockDataProvider.getUserNotifications().getOrDefault(userId, new ArrayList<>());
    }
    
    @Override
    public Integer getUnreadCount(String userId) {
        return (int) findAllForUser(userId).stream()
            .filter(n -> !Boolean.TRUE.equals(n.getRead()))
            .count();
    }
    
    @Override
    public void markAsRead(String notificationId) {
        mockDataProvider.getUserNotifications().values().stream()
            .flatMap(List::stream)
            .filter(n -> notificationId.equals(n.getId()))
            .findFirst()
            .ifPresent(n -> n.setRead(true));
    }
    
    @Override
    public void markAllAsRead(String userId) {
        findAllForUser(userId).forEach(n -> n.setRead(true));
    }
}
