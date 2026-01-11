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
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;

/**
 * Settlement management endpoints.
 */
@Path("/api/workspaces/{workspaceId}/settlements")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class SettlementResource {

    @Inject
    private SettlementService settlementService;

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    public Response listSettlements(@PathParam("workspaceId") String workspaceId) {
        List<Settlement> settlements = settlementService.findForWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlements);
        return Response.ok(response).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSettlement(@PathParam("workspaceId") String workspaceId,
                                     @Valid CreateSettlementRequest request) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        Settlement settlement = settlementService.create(workspaceId, userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", settlement);
        return Response.ok(response).build();
    }
}
