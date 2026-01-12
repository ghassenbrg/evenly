package io.evenly.core.domain.repository;

import io.evenly.core.domain.Invite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Invite domain entities.
 * Port in the ports & adapters architecture.
 */
public interface InviteRepository {
    Optional<Invite> findById(UUID id);
    Optional<Invite> findByCode(String code);
    List<Invite> findByWorkspaceId(UUID workspaceId);
    Invite save(Invite invite);
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean existsByCode(String code);
}
