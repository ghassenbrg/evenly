package io.evenly.core.features.settlements;

import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;

import java.util.List;

/**
 * Service interface for settlement operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface SettlementService {
    List<Settlement> findForWorkspace(String workspaceId);
    Settlement create(String workspaceId, String userId, CreateSettlementRequest request);
}
