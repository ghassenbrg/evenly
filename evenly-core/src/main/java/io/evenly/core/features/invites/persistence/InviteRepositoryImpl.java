package io.evenly.core.features.invites.persistence;

import io.evenly.core.domain.Invite;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of InviteRepository.
 * Repositories do not manage transactions - services own transaction boundaries.
 */
@ApplicationScoped
public class InviteRepositoryImpl implements io.evenly.core.domain.repository.InviteRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Invite> findById(UUID id) {
        Invite invite = entityManager.find(Invite.class, id);
        return Optional.ofNullable(invite);
    }

    @Override
    public Optional<Invite> findByCode(String code) {
        return entityManager.createQuery(
            "SELECT i FROM Invite i WHERE i.code = :code", Invite.class)
            .setParameter("code", code)
            .getResultStream()
            .findFirst();
    }

    @Override
    public List<Invite> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT i FROM Invite i WHERE i.workspaceId = :workspaceId ORDER BY i.createdAt DESC", 
            Invite.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public Invite save(Invite invite) {
        if (invite.getId() == null) {
            invite.setId(UUID.randomUUID());
            if (invite.getCreatedAt() == null) {
                invite.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (invite.getUsesCount() == null) {
                invite.setUsesCount(0);
            }
            entityManager.persist(invite);
            return invite;
        } else {
            return entityManager.merge(invite);
        }
    }

    @Override
    public void delete(UUID id) {
        Invite invite = entityManager.find(Invite.class, id);
        if (invite != null) {
            entityManager.remove(invite);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(i) FROM Invite i WHERE i.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByCode(String code) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(i) FROM Invite i WHERE i.code = :code", Long.class)
            .setParameter("code", code)
            .getSingleResult();
        return count > 0;
    }
}
