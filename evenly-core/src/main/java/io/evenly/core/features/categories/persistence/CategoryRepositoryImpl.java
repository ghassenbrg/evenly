package io.evenly.core.features.categories.persistence;

import io.evenly.core.domain.Category;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-based implementation of CategoryRepository.
 */
@ApplicationScoped
public class CategoryRepositoryImpl implements io.evenly.core.domain.repository.CategoryRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public Optional<Category> findById(UUID id) {
        Category category = entityManager.find(Category.class, id);
        return Optional.ofNullable(category);
    }

    @Override
    public List<Category> findByWorkspaceId(UUID workspaceId) {
        return entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.workspaceId = :workspaceId AND c.isActive = true ORDER BY c.sortOrder, c.name", 
            Category.class)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    @Override
    public List<Category> findGlobalCategories() {
        return entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.workspaceId IS NULL AND c.isActive = true ORDER BY c.sortOrder, c.name", 
            Category.class)
            .getResultList();
    }

    @Override
    public Optional<Category> findByWorkspaceIdAndSlug(UUID workspaceId, String slug) {
        return entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.workspaceId = :workspaceId AND c.slug = :slug", 
            Category.class)
            .setParameter("workspaceId", workspaceId)
            .setParameter("slug", slug)
            .getResultStream()
            .findFirst();
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.workspaceId IS NULL AND c.slug = :slug", 
            Category.class)
            .setParameter("slug", slug)
            .getResultStream()
            .findFirst();
    }

    @Override
    @Transactional
    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID());
            if (category.getCreatedAt() == null) {
                category.setCreatedAt(java.time.OffsetDateTime.now());
            }
            if (category.getUpdatedAt() == null) {
                category.setUpdatedAt(java.time.OffsetDateTime.now());
            }
            if (category.getIsActive() == null) {
                category.setIsActive(true);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            entityManager.persist(category);
            return category;
        } else {
            category.setUpdatedAt(java.time.OffsetDateTime.now());
            return entityManager.merge(category);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM Category c WHERE c.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM Category c WHERE c.workspaceId = :workspaceId AND c.slug = :slug", Long.class)
            .setParameter("workspaceId", workspaceId)
            .setParameter("slug", slug)
            .getSingleResult();
        return count > 0;
    }
}
