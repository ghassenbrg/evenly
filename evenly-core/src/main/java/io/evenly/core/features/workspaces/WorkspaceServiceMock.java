package io.evenly.core.features.workspaces;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.features.workspaces.dto.CreateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceSettingsRequest;
import io.evenly.core.features.workspaces.dto.UpdateMemberWeightsRequest;
import io.evenly.core.shared.common.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of WorkspaceService.
 * Provides realistic mock data and business logic that can be easily swapped with real persistence.
 */
@ApplicationScoped
public class WorkspaceServiceMock implements WorkspaceService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Workspace> findAllForUser(String userId) {
        return mockDataProvider.getUserWorkspaces().getOrDefault(userId, new ArrayList<>());
    }
    
    @Override
    public Optional<Workspace> findById(String workspaceId) {
        return Optional.ofNullable(mockDataProvider.getWorkspaces().get(workspaceId));
    }
    
    @Override
    public Workspace create(String userId, CreateWorkspaceRequest request) {
        String workspaceId = "ws-" + UUID.randomUUID().toString().substring(0, 8);
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setName(request.getName());
        workspace.setDefaultSplitMode(request.getDefaultSplitMode());
        workspace.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        workspace.setMonthlySharedLimit(request.getMonthlySharedLimit());
        workspace.setIsPersonal(false);
        workspace.setCreatedAt(OffsetDateTime.now());
        workspace.setUpdatedAt(OffsetDateTime.now());
        
        mockDataProvider.getWorkspaces().put(workspaceId, workspace);
        mockDataProvider.getUserWorkspaces().computeIfAbsent(userId, k -> new ArrayList<>()).add(workspace);
        
        // Add creator as owner member
        User user = mockDataProvider.getUsers().get(userId);
        if (user != null) {
            List<WorkspaceMember> members = new ArrayList<>();
            WorkspaceMember member = new WorkspaceMember();
            member.setUserId(userId);
            member.setRole("OWNER");
            member.setUser(user);
            member.setWeightPercent(100.0);
            members.add(member);
            mockDataProvider.getWorkspaceMembers().put(workspaceId, members);
        }
        
        return workspace;
    }
    
    @Override
    public Workspace update(String workspaceId, UpdateWorkspaceRequest request) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDefaultSplitMode() != null) {
            workspace.setDefaultSplitMode(request.getDefaultSplitMode());
        }
        if (request.getMonthlySharedLimit() != null) {
            workspace.setMonthlySharedLimit(request.getMonthlySharedLimit());
        }
        workspace.setUpdatedAt(OffsetDateTime.now());
        
        return workspace;
    }
    
    @Override
    public void delete(String workspaceId) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        // Check if workspace has expenses (simulate conflict check)
        List<io.evenly.core.features.expenses.dto.Expense> expenses = mockDataProvider.getWorkspaceExpenses().get(workspaceId);
        if (expenses != null && !expenses.isEmpty()) {
            throw new RuntimeException("Cannot delete workspace with expenses");
        }
        
        mockDataProvider.getWorkspaces().remove(workspaceId);
        mockDataProvider.getWorkspaceMembers().remove(workspaceId);
        mockDataProvider.getWorkspaceCategories().remove(workspaceId);
        mockDataProvider.getWorkspaceExpenses().remove(workspaceId);
        mockDataProvider.getWorkspacePayments().remove(workspaceId);
        mockDataProvider.getWorkspaceSettlements().remove(workspaceId);
        
        // Remove from user workspaces
        mockDataProvider.getUserWorkspaces().values().forEach(workspaces -> workspaces.removeIf(ws -> ws.getId().equals(workspaceId)));
    }
    
    @Override
    public Workspace updateSettings(String workspaceId, UpdateWorkspaceSettingsRequest request) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDefaultSplitMode() != null) {
            workspace.setDefaultSplitMode(request.getDefaultSplitMode());
        }
        if (request.getMonthlySharedLimit() != null) {
            workspace.setMonthlySharedLimit(request.getMonthlySharedLimit());
        }
        workspace.setUpdatedAt(OffsetDateTime.now());
        
        return workspace;
    }
    
    @Override
    public List<WorkspaceMember> findMembers(String workspaceId) {
        return mockDataProvider.getWorkspaceMembers().getOrDefault(workspaceId, new ArrayList<>());
    }
    
    @Override
    public void updateMemberWeights(String workspaceId, UpdateMemberWeightsRequest request) {
        List<WorkspaceMember> members = mockDataProvider.getWorkspaceMembers().get(workspaceId);
        if (members == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        Map<String, UpdateMemberWeightsRequest.MemberWeight> weightMap = request.getWeights().stream()
            .collect(Collectors.toMap(UpdateMemberWeightsRequest.MemberWeight::getUserId, w -> w));
        
        for (WorkspaceMember member : members) {
            UpdateMemberWeightsRequest.MemberWeight weight = weightMap.get(member.getUserId());
            if (weight != null) {
                member.setWeightPercent(weight.getWeightPercent());
                if (weight.getPersonalMonthlyLimit() != null) {
                    member.setPersonalMonthlyLimit(weight.getPersonalMonthlyLimit());
                }
            }
        }
    }
}
