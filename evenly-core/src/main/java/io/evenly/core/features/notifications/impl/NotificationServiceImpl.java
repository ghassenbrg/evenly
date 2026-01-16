package io.evenly.core.features.notifications.impl;

import io.evenly.core.domain.NotificationEntityType;
import io.evenly.core.domain.NotificationType;
import io.evenly.core.domain.repository.NotificationRepository;
import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.features.notifications.dto.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationServiceImpl implements io.evenly.core.features.notifications.NotificationService {

    @Inject
    private NotificationRepository notificationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Notification> findAllForUser(String userId) { // userId is now username (String)
        List<io.evenly.core.domain.Notification> domainNotifications = notificationRepository.findByRecipientUserId(userId);
        return domainNotifications.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Integer getUnreadCount(String userId) { // userId is now username (String)
        return (int) notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        UUID notificationUuid = UUID.fromString(notificationId);
        notificationRepository.markAsRead(notificationUuid);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) { // userId is now username (String)
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void notifyExpenseEvent(String workspaceId, String actorUserId, String expenseId, List<String> participantUserIds,
                                   NotificationType type, String detail) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        List<String> recipients = participantUserIds.stream()
            .filter(userId -> userId != null && !userId.equals(actorUserId))
            .distinct()
            .collect(Collectors.toList());

        String title = switch (type) {
            case EXPENSE_CREATED -> "Expense created";
            case EXPENSE_UPDATED -> "Expense updated";
            case EXPENSE_DELETED -> "Expense deleted";
            default -> "Expense update";
        };
        String message = detail != null && !detail.isBlank() ? detail : "Expense updated";

        for (String recipientId : recipients) {
            notificationRepository.save(buildNotification(workspaceUuid, recipientId, actorUserId, type,
                title, message, NotificationEntityType.EXPENSE, expenseId));
        }
    }

    @Override
    @Transactional
    public void notifyPaymentEvent(String workspaceId, String actorUserId, String paymentId, String otherPartyUserId,
                                   NotificationType type, String detail) {
        if (otherPartyUserId == null || otherPartyUserId.equals(actorUserId)) {
            return;
        }
        UUID workspaceUuid = UUID.fromString(workspaceId);
        String title = switch (type) {
            case PAYMENT_CREATED -> "Payment created";
            case PAYMENT_UPDATED -> "Payment updated";
            case PAYMENT_DELETED -> "Payment deleted";
            default -> "Payment update";
        };
        String message = detail != null && !detail.isBlank() ? detail : "Payment updated";
        notificationRepository.save(buildNotification(workspaceUuid, otherPartyUserId, actorUserId, type,
            title, message, NotificationEntityType.PAYMENT, paymentId));
    }

    @Override
    @Transactional
    public void notifyWorkspaceUpdated(String workspaceId, String actorUserId, String message) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        List<String> memberIds = workspaceMemberRepository.findByWorkspaceId(workspaceUuid).stream()
            .map(io.evenly.core.domain.WorkspaceMember::getUserId)
            .filter(userId -> userId != null && !userId.equals(actorUserId))
            .distinct()
            .collect(Collectors.toList());

        String title = "Workspace updated";
        String finalMessage = message != null && !message.isBlank() ? message : "Workspace settings updated";

        for (String recipientId : memberIds) {
            notificationRepository.save(buildNotification(workspaceUuid, recipientId, actorUserId,
                NotificationType.WORKSPACE_UPDATED, title, finalMessage,
                NotificationEntityType.WORKSPACE, workspaceId));
        }
    }

    @Override
    @Transactional
    public void notifyMemberJoined(String workspaceId, String actorUserId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        List<String> memberIds = workspaceMemberRepository.findByWorkspaceId(workspaceUuid).stream()
            .map(io.evenly.core.domain.WorkspaceMember::getUserId)
            .filter(userId -> userId != null && !userId.equals(actorUserId))
            .distinct()
            .collect(Collectors.toList());

        String title = "New member joined";
        String message = "Joined the workspace.";

        for (String recipientId : memberIds) {
            notificationRepository.save(buildNotification(workspaceUuid, recipientId, actorUserId,
                NotificationType.WORKSPACE_MEMBER_JOINED, title, message,
                NotificationEntityType.WORKSPACE, workspaceId));
        }
    }

    @Override
    @Transactional
    public void checkBudgetThresholds(String workspaceId, String actorUserId, LocalDate effectiveDate,
                                      java.math.BigDecimal workspaceTotalPaid, java.math.BigDecimal budgetLimit) {
        if (budgetLimit == null || budgetLimit.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return;
        }
        if (workspaceTotalPaid == null) {
            return;
        }

        java.math.BigDecimal percentage = workspaceTotalPaid
            .divide(budgetLimit, 4, java.math.RoundingMode.HALF_UP)
            .multiply(java.math.BigDecimal.valueOf(100));

        YearMonth cycle = YearMonth.from(effectiveDate != null ? effectiveDate : LocalDate.now());
        String cycleKey = cycle.toString();

        if (percentage.compareTo(java.math.BigDecimal.valueOf(100)) >= 0) {
            createBudgetThresholdNotifications(workspaceId, actorUserId, cycleKey,
                NotificationType.BUDGET_LIMIT_REACHED, "Budget limit reached",
                "Budget limit reached for this month.");
        } else if (percentage.compareTo(java.math.BigDecimal.valueOf(80)) >= 0) {
            createBudgetThresholdNotifications(workspaceId, actorUserId, cycleKey,
                NotificationType.BUDGET_THRESHOLD_80, "Budget at 80%",
                "Budget usage has reached 80% for this month.");
        }
    }

    private Notification toDto(io.evenly.core.domain.Notification domain) {
        Notification dto = new Notification();
        dto.setId(domain.getId().toString());
        dto.setWorkspaceId(domain.getWorkspaceId() != null ? domain.getWorkspaceId().toString() : null);
        dto.setRecipientUserId(domain.getRecipientUserId());
        dto.setActorUserId(domain.getActorUserId());
        dto.setType(domain.getType() != null ? domain.getType().name() : null);
        dto.setTitle(domain.getTitle());
        dto.setMessage(domain.getMessage());
        dto.setContent(domain.getMessage());
        dto.setEntityType(domain.getEntityType() != null ? domain.getEntityType().name() : null);
        dto.setEntityId(domain.getEntityId());
        dto.setRead(domain.getIsRead());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setTimestamp(domain.getCreatedAt());
        return dto;
    }

    private io.evenly.core.domain.Notification buildNotification(UUID workspaceId, String recipientUserId, String actorUserId,
                                                                 NotificationType type, String title, String message,
                                                                 NotificationEntityType entityType, String entityId) {
        return io.evenly.core.domain.Notification.builder()
            .workspaceId(workspaceId)
            .recipientUserId(recipientUserId)
            .actorUserId(actorUserId)
            .type(type)
            .title(title)
            .message(message)
            .entityType(entityType)
            .entityId(entityId)
            .isRead(false)
            .createdAt(OffsetDateTime.now())
            .build();
    }

    private void createBudgetThresholdNotifications(String workspaceId, String actorUserId, String cycleKey,
                                                    NotificationType type, String title, String message) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        List<String> memberIds = workspaceMemberRepository.findByWorkspaceId(workspaceUuid).stream()
            .map(io.evenly.core.domain.WorkspaceMember::getUserId)
            .filter(userId -> userId != null)
            .distinct()
            .collect(Collectors.toList());

        for (String recipientId : memberIds) {
            if (notificationRepository.existsByRecipientAndTypeAndEntity(recipientId, type, NotificationEntityType.BUDGET, cycleKey)) {
                continue;
            }
            notificationRepository.save(buildNotification(workspaceUuid, recipientId, actorUserId,
                type, title, message, NotificationEntityType.BUDGET, cycleKey));
        }
    }

    private String resolveDisplayName(String userId) {
        if (userId == null) {
            return "Someone";
        }
        return userRepository.findById(userId)
            .map(io.evenly.core.domain.User::getDisplayName)
            .orElse("Someone");
    }

    private String actionText(NotificationType type) {
        return switch (type) {
            case EXPENSE_CREATED, PAYMENT_CREATED -> "created";
            case EXPENSE_UPDATED, PAYMENT_UPDATED -> "updated";
            case EXPENSE_DELETED, PAYMENT_DELETED -> "deleted";
            default -> "updated";
        };
    }
}
