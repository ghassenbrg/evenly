package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Category;
import io.evenly.core.shared.dto.request.CreateCategoryRequest;
import io.evenly.core.shared.dto.request.UpdateCategoryRequest;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for category operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface CategoryService {
    List<Category> findAllGlobal();
    List<Category> findAllForWorkspace(String workspaceId);
    Optional<Category> findById(String categoryId);
    Category createGlobal(CreateCategoryRequest request);
    Category createForWorkspace(String workspaceId, CreateCategoryRequest request);
    Category update(String categoryId, UpdateCategoryRequest request);
    void delete(String categoryId);
}
