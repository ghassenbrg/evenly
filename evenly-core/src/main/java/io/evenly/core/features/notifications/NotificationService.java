package io.evenly.core.features.notifications;

import io.evenly.core.domain.NotificationType;
import io.evenly.core.features.notifications.dto.Notification;

import java.time.LocalDate;
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
    void notifyExpenseEvent(String workspaceId, String actorUserId, String expenseId, List<String> participantUserIds,
                            NotificationType type);
    void notifyPaymentEvent(String workspaceId, String actorUserId, String paymentId, String otherPartyUserId,
                            NotificationType type);
    void notifyWorkspaceUpdated(String workspaceId, String actorUserId, String message);
    void notifyMemberJoined(String workspaceId, String actorUserId);
    void checkBudgetThresholds(String workspaceId, String actorUserId, LocalDate effectiveDate,
                               java.math.BigDecimal workspaceTotalPaid, java.math.BigDecimal budgetLimit);
}
