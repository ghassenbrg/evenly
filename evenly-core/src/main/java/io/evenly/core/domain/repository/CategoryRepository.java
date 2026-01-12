package io.evenly.core.domain.repository;

import io.evenly.core.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category domain entities.
 * Port in the ports & adapters architecture.
 */
public interface CategoryRepository {
    Optional<Category> findById(UUID id);
    List<Category> findByWorkspaceId(UUID workspaceId);
    List<Category> findGlobalCategories();
    Optional<Category> findByWorkspaceIdAndSlug(UUID workspaceId, String slug);
    Optional<Category> findBySlug(String slug); // For global categories
    Category save(Category category);
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);
}
