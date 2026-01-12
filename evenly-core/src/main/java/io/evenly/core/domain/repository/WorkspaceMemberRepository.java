package io.evenly.core.domain.repository;

import io.evenly.core.domain.WorkspaceMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for WorkspaceMember domain entities.
 * Port in the ports & adapters architecture.
 */
public interface WorkspaceMemberRepository {
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, String userId); // userId is now String (username)
    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);
    List<WorkspaceMember> findByUserId(String userId); // userId is now String (username)
    WorkspaceMember save(WorkspaceMember member);
    void delete(UUID workspaceId, String userId); // userId is now String (username)
    void deleteByWorkspaceId(UUID workspaceId);
    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, String userId); // userId is now String (username)
    boolean hasOwner(UUID workspaceId);
    List<WorkspaceMember> findAllByWorkspaceId(UUID workspaceId);
}
