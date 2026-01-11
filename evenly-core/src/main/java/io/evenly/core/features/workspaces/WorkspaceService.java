package io.evenly.core.features.workspaces;

import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.features.workspaces.dto.CreateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceSettingsRequest;
import io.evenly.core.features.workspaces.dto.UpdateMemberWeightsRequest;

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
