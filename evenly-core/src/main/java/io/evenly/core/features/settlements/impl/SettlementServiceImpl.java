package io.evenly.core.features.settlements.impl;

import io.evenly.core.domain.Payment;
import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SettlementService implementation.
 * Note: Based on the schema, settlements and transfers were removed.
 * This service works with payments instead.
 */
@ApplicationScoped
public class SettlementServiceImpl implements io.evenly.core.features.settlements.SettlementService {

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Settlement> findForWorkspace(String workspaceId) {
        // Since settlements table was removed, we return empty list
        // or could return payments as settlements
        return List.of();
    }

    @Override
    @Transactional
    public Settlement create(String workspaceId, String userId, CreateSettlementRequest request) { // userId is now username (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get workspace to get currency
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));

        // TODO: Settlement creation needs more information (fromUserId, toUserId, amount)
        // For now, return a placeholder settlement
        // In a real implementation, this would calculate balances and create transfers
        Settlement result = new Settlement();
        result.setId(UUID.randomUUID().toString());
        result.setWorkspaceId(workspaceId);
        result.setCreatedByUserId(userId);
        result.setCreatedAt(java.time.OffsetDateTime.now());
        return result;
    }
}
