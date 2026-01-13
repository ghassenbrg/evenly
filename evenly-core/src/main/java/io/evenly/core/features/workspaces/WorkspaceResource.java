package io.evenly.core.features.workspaces;

import io.evenly.core.shared.exception.ConflictException;
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
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.CreateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceSettingsRequest;
import io.evenly.core.features.workspaces.dto.UpdateMemberWeightsRequest;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;

/**
 * Workspace management endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class WorkspaceResource {

    @Inject
    private WorkspaceService workspaceService;

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    @Path("/workspaces")
    public Response listWorkspaces() {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        List<Workspace> workspaces = workspaceService.findAllForUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", workspaces);
        return Response.ok(response).build();
    }

    @POST
    @Path("/workspaces")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createWorkspace(@Valid CreateWorkspaceRequest request) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        Workspace workspace = workspaceService.create(userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", workspace);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        Optional<Workspace> workspace = workspaceService.findById(workspaceId);
        if (workspace.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        // Get members for this workspace
        List<WorkspaceMember> members = workspaceService.findMembers(workspaceId);
        
        // Create response with workspace data and members
        Map<String, Object> workspaceData = new HashMap<>();
        workspaceData.put("id", workspace.get().getId());
        workspaceData.put("name", workspace.get().getName());
        workspaceData.put("defaultSplitMode", workspace.get().getDefaultSplitMode());
        workspaceData.put("monthlySharedLimit", workspace.get().getMonthlySharedLimit());
        workspaceData.put("isPersonal", workspace.get().getIsPersonal());
        workspaceData.put("currency", workspace.get().getCurrency());
        workspaceData.put("inviteCode", workspace.get().getInviteCode());
        workspaceData.put("inviteLink", workspace.get().getInviteLink());
        if (workspace.get().getCreatedAt() != null) {
            workspaceData.put("createdAt", workspace.get().getCreatedAt());
        }
        if (workspace.get().getUpdatedAt() != null) {
            workspaceData.put("updatedAt", workspace.get().getUpdatedAt());
        }
        workspaceData.put("members", members);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", workspaceData);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/workspaces/{workspaceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateWorkspace(@PathParam("workspaceId") String workspaceId,
                                    @Valid UpdateWorkspaceRequest request) {
        Optional<Workspace> existing = workspaceService.findById(workspaceId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        Workspace workspace = workspaceService.update(workspaceId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("workspace", workspace);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/workspaces/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        Optional<Workspace> existing = workspaceService.findById(workspaceId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        try {
            workspaceService.delete(workspaceId);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot delete")) {
                throw new ConflictException("Workspace cannot be deleted");
            }
            throw e;
        }
    }

    @PUT
    @Path("/workspaces/{workspaceId}/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSettings(@PathParam("workspaceId") String workspaceId,
                                   @Valid UpdateWorkspaceSettingsRequest request) {
        Optional<Workspace> existing = workspaceService.findById(workspaceId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        Workspace workspace = workspaceService.updateSettings(workspaceId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("workspace", workspace);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/members")
    public Response listMembers(@PathParam("workspaceId") String workspaceId) {
        Optional<Workspace> existing = workspaceService.findById(workspaceId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        List<WorkspaceMember> members = workspaceService.findMembers(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", members);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/workspaces/{workspaceId}/members/weights")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMemberWeights(@PathParam("workspaceId") String workspaceId,
                                        @Valid UpdateMemberWeightsRequest request) {
        Optional<Workspace> existing = workspaceService.findById(workspaceId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Workspace not found");
        }
        
        workspaceService.updateMemberWeights(workspaceId, request);
        return Response.ok().build();
    }
}
