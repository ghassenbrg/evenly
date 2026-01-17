package io.evenly.core.domain.repository;

import io.evenly.core.domain.Settlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Settlement domain entities.
 */
public interface SettlementRepository {
    Optional<Settlement> findById(UUID id);
    List<Settlement> findByWorkspaceId(UUID workspaceId);
    Settlement save(Settlement settlement);
}
