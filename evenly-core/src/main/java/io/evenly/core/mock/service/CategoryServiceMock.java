package io.evenly.core.mock.service;

import io.evenly.core.features.categories.CategoryService;
import io.evenly.core.features.categories.dto.Category;
import io.evenly.core.features.categories.dto.CreateCategoryRequest;
import io.evenly.core.features.categories.dto.UpdateCategoryRequest;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of CategoryService.
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class CategoryServiceMock implements CategoryService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public List<Category> findAllGlobal() {
        return mockDataProvider.getGlobalCategories();
    }
    
    @Override
    public List<Category> findAllForWorkspace(String workspaceId) {
        return mockDataProvider.getWorkspaceCategories().getOrDefault(workspaceId, new ArrayList<>());
    }
    
    @Override
    public Optional<Category> findById(String categoryId) {
        // Check global categories
        for (Category cat : mockDataProvider.getGlobalCategories()) {
            if (cat.getId().equals(categoryId)) {
                return Optional.of(cat);
            }
        }
        // Check workspace categories
        for (List<Category> categories : mockDataProvider.getWorkspaceCategories().values()) {
            for (Category cat : categories) {
                if (cat.getId().equals(categoryId)) {
                    return Optional.of(cat);
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Category createGlobal(CreateCategoryRequest request) {
        Category category = new Category();
        category.setId("global-cat-" + UUID.randomUUID().toString().substring(0, 8));
        category.setName(request.getName());
        category.setSlug(request.getName().toLowerCase().replace(" ", "-"));
        category.setIcon(request.getIcon());
        category.setColor(request.getColor() != null ? request.getColor() : "#85C1E2");
        category.setIsActive(true);
        category.setSortOrder(mockDataProvider.getGlobalCategories().size() + 1);
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());
        mockDataProvider.getGlobalCategories().add(category);
        return category;
    }
    
    @Override
    public Category createForWorkspace(String workspaceId, CreateCategoryRequest request) {
        Category category = new Category();
        category.setId(workspaceId + "-cat-" + UUID.randomUUID().toString().substring(0, 8));
        category.setWorkspaceId(workspaceId);
        category.setName(request.getName());
        category.setSlug(request.getName().toLowerCase().replace(" ", "-"));
        category.setIcon(request.getIcon());
        category.setColor(request.getColor() != null ? request.getColor() : "#85C1E2");
        category.setIsActive(true);
        List<Category> categories = mockDataProvider.getWorkspaceCategories().computeIfAbsent(workspaceId, k -> new ArrayList<>());
        category.setSortOrder(categories.size() + 1);
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());
        categories.add(category);
        return category;
    }
    
    @Override
    public Category update(String categoryId, UpdateCategoryRequest request) {
        Optional<Category> optCategory = findById(categoryId);
        if (optCategory.isEmpty()) {
            throw new RuntimeException("Category not found");
        }
        Category category = optCategory.get();
        
        if (request.getName() != null) {
            category.setName(request.getName());
            category.setSlug(request.getName().toLowerCase().replace(" ", "-"));
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        category.setUpdatedAt(OffsetDateTime.now());
        
        return category;
    }
    
    @Override
    public void delete(String categoryId) {
        Optional<Category> optCategory = findById(categoryId);
        if (optCategory.isEmpty()) {
            throw new RuntimeException("Category not found");
        }
        
        // Remove from global
        mockDataProvider.getGlobalCategories().removeIf(c -> c.getId().equals(categoryId));
        
        // Remove from workspace categories
        mockDataProvider.getWorkspaceCategories().values().forEach(categories -> 
            categories.removeIf(c -> c.getId().equals(categoryId)));
    }
}
