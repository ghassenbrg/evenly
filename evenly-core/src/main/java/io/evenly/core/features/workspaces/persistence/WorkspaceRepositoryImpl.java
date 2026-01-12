package io.evenly.core.features.workspaces.persistence;

import io.evenly.core.domain.Workspace;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of WorkspaceRepository.
 * Clean and simple - no manual JDBC code!
 */
@ApplicationScoped
public class WorkspaceRepositoryImpl implements io.evenly.core.domain.repository.WorkspaceRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Workspace> findById(UUID id) {
        Workspace workspace = entityManager.find(Workspace.class, id);
        return Optional.ofNullable(workspace);
    }

    @Override
    public List<Workspace> findByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT w FROM Workspace w " +
            "INNER JOIN WorkspaceMember wm ON w.id = wm.workspaceId " +
            "WHERE wm.userId = :userId " +
            "ORDER BY w.createdAt DESC", Workspace.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    @Transactional
    public Workspace save(Workspace workspace) {
        if (workspace.getId() == null) {
            workspace.setId(UUID.randomUUID());
            if (workspace.getCreatedAt() == null) {
                workspace.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (workspace.getUpdatedAt() == null) {
                workspace.setUpdatedAt(java.time.OffsetDateTime.now());
            }
            entityManager.persist(workspace);
            return workspace;
        } else {
            workspace.setUpdatedAt(java.time.OffsetDateTime.now());
            return entityManager.merge(workspace);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Workspace workspace = entityManager.find(Workspace.class, id);
        if (workspace != null) {
            entityManager.remove(workspace);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return entityManager.createQuery(
            "SELECT COUNT(w) > 0 FROM Workspace w WHERE w.id = :id", Boolean.class)
            .setParameter("id", id)
            .getSingleResult();
    }

    @Override
    public boolean hasExpenses(UUID workspaceId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(e) FROM Expense e WHERE e.workspaceId = :workspaceId", Long.class)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean hasPayments(UUID workspaceId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(p) FROM Payment p WHERE p.workspaceId = :workspaceId", Long.class)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult();
        return count > 0;
    }
}
