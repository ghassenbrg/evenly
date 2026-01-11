package io.evenly.core.features.categories;

import io.evenly.core.shared.exception.NotFoundException;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.evenly.core.features.categories.dto.Category;
import io.evenly.core.features.categories.dto.CreateCategoryRequest;
import io.evenly.core.features.categories.dto.UpdateCategoryRequest;

/**
 * Category management endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class CategoryResource {

    @Inject
    private CategoryService categoryService;

    @Inject
    private SecurityContextProvider securityContext;

    // Global categories
    @GET
    @Path("/categories")
    public Response listGlobalCategories() {
        List<Category> categories = categoryService.findAllGlobal();
        Map<String, Object> response = new HashMap<>();
        response.put("data", categories);
        return Response.ok(response).build();
    }

    @POST
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGlobalCategory(@Valid CreateCategoryRequest request) {
        Category category = categoryService.createGlobal(request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", category);
        return Response.ok(response).build();
    }

    @GET
    @Path("/categories/{categoryId}")
    public Response getCategory(@PathParam("categoryId") String categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", category.get());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCategory(@PathParam("categoryId") String categoryId,
                                   @Valid UpdateCategoryRequest request) {
        Optional<Category> existing = categoryService.findById(categoryId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        Category category = categoryService.update(categoryId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", category);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/categories/{categoryId}")
    public Response deleteCategory(@PathParam("categoryId") String categoryId) {
        Optional<Category> existing = categoryService.findById(categoryId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        categoryService.delete(categoryId);
        return Response.noContent().build();
    }

    // Workspace-scoped categories
    @GET
    @Path("/workspaces/{workspaceId}/categories")
    public Response listWorkspaceCategories(@PathParam("workspaceId") String workspaceId) {
        List<Category> categories = categoryService.findAllForWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", categories);
        return Response.ok(response).build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createWorkspaceCategory(@PathParam("workspaceId") String workspaceId,
                                           @Valid CreateCategoryRequest request) {
        Category category = categoryService.createForWorkspace(workspaceId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", category);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/categories/{categoryId}")
    public Response getWorkspaceCategory(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("categoryId") String categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", category.get());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/workspaces/{workspaceId}/categories/{categoryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateWorkspaceCategory(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("categoryId") String categoryId,
                                            @Valid UpdateCategoryRequest request) {
        Optional<Category> existing = categoryService.findById(categoryId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        Category category = categoryService.update(categoryId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", category);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/workspaces/{workspaceId}/categories/{categoryId}")
    public Response deleteWorkspaceCategory(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("categoryId") String categoryId) {
        Optional<Category> existing = categoryService.findById(categoryId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        categoryService.delete(categoryId);
        return Response.noContent().build();
    }
}
