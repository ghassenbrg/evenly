package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Settlement;
import io.evenly.core.shared.dto.request.CreateSettlementRequest;

import java.util.List;

/**
 * Service interface for settlement operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface SettlementService {
    List<Settlement> findForWorkspace(String workspaceId);
    Settlement create(String workspaceId, String userId, CreateSettlementRequest request);
}
