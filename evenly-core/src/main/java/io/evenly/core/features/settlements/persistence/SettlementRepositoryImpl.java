package io.evenly.core.features.settlements.persistence;

import io.evenly.core.domain.Settlement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of SettlementRepository.
 */
@ApplicationScoped
public class SettlementRepositoryImpl implements io.evenly.core.domain.repository.SettlementRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Settlement> findById(UUID id) {
        Settlement settlement = entityManager.find(Settlement.class, id);
        return Optional.ofNullable(settlement);
    }

    @Override
    public List<Settlement> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
                "SELECT s FROM Settlement s WHERE s.workspaceId = :workspaceId ORDER BY s.createdAt DESC",
                Settlement.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public Settlement save(Settlement settlement) {
        if (settlement.getId() == null) {
            settlement.setId(UUID.randomUUID());
            if (settlement.getCreatedAt() == null) {
                settlement.setCreatedAt(java.time.OffsetDateTime.now());
            }
            entityManager.persist(settlement);
            return settlement;
        }
        return entityManager.merge(settlement);
    }
}
