package io.evenly.core.mock.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.evenly.core.domain.NotificationType;
import io.evenly.core.features.notifications.NotificationService;
import io.evenly.core.features.notifications.dto.Notification;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

/**
 * \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class NotificationServiceMock implements NotificationService {

    @Inject
    private MockDataProvider mockDataProvider;

    @Override
    public List<Notification> findAllForUser(String userId) {
        // For demo purposes, return notifications for "gbargougui" for any
        // authenticated user
        List<Notification> userNotifications = mockDataProvider.getUserNotifications().getOrDefault(userId,
                new ArrayList<>());

        // If no notifications found for this user, return demo notifications for
        // "gbargougui"
        if (userNotifications.isEmpty()) {
            userNotifications = mockDataProvider.getUserNotifications().getOrDefault("gbargougui", new ArrayList<>());
        }

        return userNotifications;
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

    @Override
    public void notifyExpenseEvent(String workspaceId, String actorUserId, String expenseId,
                                   List<String> participantUserIds, NotificationType type) {
    }

    @Override
    public void notifyPaymentEvent(String workspaceId, String actorUserId, String paymentId, String otherPartyUserId,
                                   NotificationType type) {
    }

    @Override
    public void notifyWorkspaceUpdated(String workspaceId, String actorUserId, String message) {
    }

    @Override
    public void notifyMemberJoined(String workspaceId, String actorUserId) {
    }

    @Override
    public void checkBudgetThresholds(String workspaceId, String actorUserId, LocalDate effectiveDate,
                                      java.math.BigDecimal workspaceTotalPaid, java.math.BigDecimal budgetLimit) {
    }
}
