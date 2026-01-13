package io.evenly.core.features.categories.impl;

import io.evenly.core.domain.repository.CategoryRepository;
import io.evenly.core.features.categories.CategoryService;
import io.evenly.core.features.categories.dto.CreateCategoryRequest;
import io.evenly.core.features.categories.dto.UpdateCategoryRequest;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CategoryServiceImpl implements CategoryService {

    @Inject
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<io.evenly.core.features.categories.dto.Category> findAllGlobal() {
        return categoryRepository.findGlobalCategories().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<io.evenly.core.features.categories.dto.Category> findAllForWorkspace(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        return categoryRepository.findByWorkspaceId(workspaceUuid).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<io.evenly.core.features.categories.dto.Category> findById(String categoryId) {
        UUID categoryUuid = UUID.fromString(categoryId);
        return categoryRepository.findById(categoryUuid)
            .map(this::toDto);
    }

    @Override
    @Transactional
    public io.evenly.core.features.categories.dto.Category createGlobal(CreateCategoryRequest request) {
        // Generate slug from name
        String slug = generateSlug(request.getName());
        
        // Check if slug already exists globally
        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Category with slug already exists: " + slug);
        }
        
        io.evenly.core.domain.Category category = io.evenly.core.domain.Category.builder()
            .name(request.getName())
            .slug(slug)
            .icon(request.getIcon())
            .color(request.getColor() != null ? request.getColor() : "#10b981")
            .workspaceId(null) // Global category
            .isActive(true)
            .sortOrder(0)
            .build();
        
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    @Transactional
    public io.evenly.core.features.categories.dto.Category createForWorkspace(String workspaceId, CreateCategoryRequest request) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        // Generate slug from name
        String slug = generateSlug(request.getName());
        
        // Check if slug already exists for this workspace
        if (categoryRepository.existsByWorkspaceIdAndSlug(workspaceUuid, slug)) {
            throw new IllegalArgumentException("Category with slug already exists in workspace: " + slug);
        }
        
        io.evenly.core.domain.Category category = io.evenly.core.domain.Category.builder()
            .workspaceId(workspaceUuid)
            .name(request.getName())
            .slug(slug)
            .icon(request.getIcon())
            .color(request.getColor() != null ? request.getColor() : "#10b981")
            .isActive(true)
            .sortOrder(0)
            .build();
        
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    @Transactional
    public io.evenly.core.features.categories.dto.Category update(String categoryId, UpdateCategoryRequest request) {
        UUID categoryUuid = UUID.fromString(categoryId);
        io.evenly.core.domain.Category category = categoryRepository.findById(categoryUuid)
            .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
        
        if (request.getName() != null) {
            category.setName(request.getName());
            // Regenerate slug if name changed
            category.setSlug(generateSlug(request.getName()));
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
        
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    @Transactional
    public void delete(String categoryId) {
        UUID categoryUuid = UUID.fromString(categoryId);
        categoryRepository.delete(categoryUuid);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    }

    private io.evenly.core.features.categories.dto.Category toDto(io.evenly.core.domain.Category domain) {
        io.evenly.core.features.categories.dto.Category dto = new io.evenly.core.features.categories.dto.Category();
        dto.setId(domain.getId().toString());
        dto.setWorkspaceId(domain.getWorkspaceId() != null ? domain.getWorkspaceId().toString() : null);
        dto.setName(domain.getName());
        dto.setSlug(domain.getSlug());
        dto.setIcon(domain.getIcon());
        dto.setColor(domain.getColor());
        dto.setIsActive(domain.getIsActive());
        dto.setSortOrder(domain.getSortOrder());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedAt(domain.getUpdatedAt());
        return dto;
    }
}
