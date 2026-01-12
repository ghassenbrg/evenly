package io.evenly.core.mock.service;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;
import io.evenly.core.features.settlements.SettlementService;

/**
 \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class SettlementServiceMock implements SettlementService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Settlement> findForWorkspace(String workspaceId) {
        return mockDataProvider.getWorkspaceSettlements().getOrDefault(workspaceId, new ArrayList<>());
    }
    
    @Override
    public Settlement create(String workspaceId, String userId, CreateSettlementRequest request) {
        Settlement settlement = new Settlement();
        settlement.setId(workspaceId + "-sett-" + UUID.randomUUID().toString().substring(0, 8));
        settlement.setWorkspaceId(workspaceId);
        settlement.setCreatedByUserId(userId);
        settlement.setCreatedAt(OffsetDateTime.now());
        
        // Get user
        User user = mockDataProvider.getUsers().get(userId);
        if (user != null) {
            settlement.setCreatedBy(user);
        }
        
        mockDataProvider.getWorkspaceSettlements().computeIfAbsent(workspaceId, k -> new ArrayList<>()).add(settlement);
        return settlement;
    }
}
