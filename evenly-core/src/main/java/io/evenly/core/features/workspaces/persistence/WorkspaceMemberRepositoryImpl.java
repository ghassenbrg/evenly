package io.evenly.core.features.workspaces.persistence;

import io.evenly.core.domain.WorkspaceMember;
import io.evenly.core.domain.WorkspaceMemberId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of WorkspaceMemberRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class WorkspaceMemberRepositoryImpl implements io.evenly.core.domain.repository.WorkspaceMemberRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, String userId) { // userId is now String (username)
        WorkspaceMemberId id = new WorkspaceMemberId(workspaceId, userId);
        WorkspaceMember member = entityManager.find(WorkspaceMember.class, id);
        return Optional.ofNullable(member);
    }

    @Override
    public List<WorkspaceMember> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT wm FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId ORDER BY wm.joinedAt", 
            WorkspaceMember.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public List<WorkspaceMember> findByUserId(String userId) { // userId is now String (username)
        return entityManager.createQuery(
            "SELECT wm FROM WorkspaceMember wm WHERE wm.userId = :userId", 
            WorkspaceMember.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public WorkspaceMember save(WorkspaceMember member) {
        if (member.getJoinedAt() == null) {
            member.setJoinedAt(java.time.OffsetDateTime.now());
        }
        if (member.getWeightPercent() == null) {
            member.setWeightPercent(new java.math.BigDecimal("100.00"));
        }
        return entityManager.merge(member);
    }

    @Override
    public void delete(UUID workspaceId, String userId) { // userId is now String (username)
        WorkspaceMemberId id = new WorkspaceMemberId(workspaceId, userId);
        WorkspaceMember member = entityManager.find(WorkspaceMember.class, id);
        if (member != null) {
            entityManager.remove(member);
        }
    }

    @Override
    public void deleteByWorkspaceId(UUID workspaceId) {
        entityManager.createQuery("DELETE FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId")
            .setParameter("workspaceId", workspaceId)
            .executeUpdate();
    }

    @Override
    public boolean existsByWorkspaceIdAndUserId(UUID workspaceId, String userId) { // userId is now String (username)
        Long count = entityManager.createQuery(
            "SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId", 
            Long.class)
            .setParameter("workspaceId", workspaceId)
            .setParameter("userId", userId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean hasOwner(UUID workspaceId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.role = 'OWNER'", 
            Long.class)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public List<WorkspaceMember> findAllByWorkspaceId(UUID workspaceId) {
        return findByWorkspaceId(workspaceId);
    }
}
