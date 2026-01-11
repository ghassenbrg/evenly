package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Workspace;
import io.evenly.core.shared.dto.WorkspaceMember;
import io.evenly.core.shared.dto.request.CreateWorkspaceRequest;
import io.evenly.core.shared.dto.request.UpdateWorkspaceRequest;
import io.evenly.core.shared.dto.request.UpdateWorkspaceSettingsRequest;
import io.evenly.core.shared.dto.request.UpdateMemberWeightsRequest;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for workspace operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface WorkspaceService {
    List<Workspace> findAllForUser(String userId);
    Optional<Workspace> findById(String workspaceId);
    Workspace create(String userId, CreateWorkspaceRequest request);
    Workspace update(String workspaceId, UpdateWorkspaceRequest request);
    void delete(String workspaceId);
    Workspace updateSettings(String workspaceId, UpdateWorkspaceSettingsRequest request);
    List<WorkspaceMember> findMembers(String workspaceId);
    void updateMemberWeights(String workspaceId, UpdateMemberWeightsRequest request);
}
