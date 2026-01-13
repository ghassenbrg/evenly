package io.evenly.core.features.notifications.impl;

import io.evenly.core.domain.repository.NotificationRepository;
import io.evenly.core.features.notifications.dto.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationServiceImpl implements io.evenly.core.features.notifications.NotificationService {

    @Inject
    private NotificationRepository notificationRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Notification> findAllForUser(String userId) { // userId is now username (String)
        List<io.evenly.core.domain.Notification> domainNotifications = notificationRepository.findByUserId(userId);
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

    private Notification toDto(io.evenly.core.domain.Notification domain) {
        Notification dto = new Notification();
        dto.setId(domain.getId().toString());
        dto.setType(domain.getType());
        dto.setContent(domain.getContent());
        dto.setRead(domain.getRead());
        dto.setTimestamp(domain.getTimestamp());
        dto.setWorkspaceId(domain.getWorkspaceId() != null ? domain.getWorkspaceId().toString() : null);
        return dto;
    }
}
