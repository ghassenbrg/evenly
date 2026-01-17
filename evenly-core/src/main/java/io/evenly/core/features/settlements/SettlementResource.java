package io.evenly.core.features.settlements;

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
import java.util.UUID;
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;
import io.evenly.core.features.settlements.dto.SettlementStatus;

/**
 * Settlement management endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class SettlementResource {

    @Inject
    private SettlementService settlementService;

    @Inject
    private SecurityContextProvider securityContext;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceMemberRepository workspaceMemberRepository;

    @GET
    @Path("/workspaces/{workspaceId}/settlements")
    public Response listSettlements(@PathParam("workspaceId") String workspaceId) {
        ensureWorkspaceAccess(workspaceId);
        List<Settlement> settlements = settlementService.findForWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlements);
        return Response.ok(response).build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/settlements")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSettlement(@PathParam("workspaceId") String workspaceId,
                                     @Valid CreateSettlementRequest request) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        ensureWorkspaceAccess(workspaceId);
        
        Settlement settlement = settlementService.create(workspaceId, userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlement);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/settlements/status")
    public Response getSettlementStatus(@PathParam("workspaceId") String workspaceId) {
        ensureWorkspaceAccess(workspaceId);
        SettlementStatus status = new SettlementStatus();
        status.setCanSuggest(settlementService.canSuggestSettlement(workspaceId));
        status.setHasUnsettled(settlementService.hasUnsettledRecords(workspaceId));
        Map<String, Object> response = new HashMap<>();
        response.put("data", status);
        return Response.ok(response).build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/settlements/settle-all")
    public Response settleAll(@PathParam("workspaceId") String workspaceId) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        ensureWorkspaceAccess(workspaceId);

        Settlement settlement = settlementService.settleAll(workspaceId, userId);
        if (settlement == null) {
            return Response.noContent().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlement);
        return Response.ok(response).build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/settlements/{settlementId}/revert")
    public Response revertSettlement(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("settlementId") String settlementId) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        ensureWorkspaceAccess(workspaceId);

        Settlement settlement = settlementService.revert(workspaceId, settlementId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlement);
        return Response.ok(response).build();
    }

    private void ensureWorkspaceAccess(String workspaceId) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
    }
}
