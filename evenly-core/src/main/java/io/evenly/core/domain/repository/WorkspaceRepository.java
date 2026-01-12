package io.evenly.core.domain.repository;

import io.evenly.core.domain.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Workspace domain entities.
 * Port in the ports & adapters architecture.
 */
public interface WorkspaceRepository {
    Optional<Workspace> findById(UUID id);
    List<Workspace> findByUserId(String userId); // userId is now String (username)
    Workspace save(Workspace workspace);
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean hasExpenses(UUID workspaceId);
    boolean hasPayments(UUID workspaceId);
}
