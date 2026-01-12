package io.evenly.core.mock.service;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.features.workspaces.dto.CreateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceSettingsRequest;
import io.evenly.core.features.workspaces.dto.UpdateMemberWeightsRequest;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import io.evenly.core.features.workspaces.WorkspaceService;

/**
 \1
 * Only active when running with the "mock" profile.
 * Provides realistic mock data and business logic that can be easily swapped with real persistence.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class WorkspaceServiceMock implements WorkspaceService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Workspace> findAllForUser(String userId) {
        // For demo purposes, return workspaces for "gbargougui" for any authenticated user
        // This allows the demo to work regardless of the actual Keycloak user ID
        List<Workspace> userWorkspaces = mockDataProvider.getUserWorkspaces().getOrDefault(userId, new ArrayList<>());
        
        // If no workspaces found for this user, return demo workspaces for "gbargougui"
        if (userWorkspaces.isEmpty()) {
            userWorkspaces = mockDataProvider.getUserWorkspaces().getOrDefault("gbargougui", new ArrayList<>());
        }
        
        return userWorkspaces;
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
