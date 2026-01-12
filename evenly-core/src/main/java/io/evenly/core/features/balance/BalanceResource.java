package io.evenly.core.features.balance;

import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.balance.dto.SettleUpResponse;

/**
 * Balance and settle-up endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class BalanceResource {

    @Inject
    private BalanceService balanceService;

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    @Path("/workspaces/{workspaceId}/balance")
    public Response getBalance(@PathParam("workspaceId") String workspaceId) {
        List<Balance> balances = balanceService.getBalanceForWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", balances);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/settle-up")
    public Response getSettleUp(@PathParam("workspaceId") String workspaceId) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        SettleUpResponse settleUp = balanceService.getSettleUpForWorkspace(workspaceId, userId);
        // Webapp expects the settle-up data directly, not wrapped
        return Response.ok(settleUp).build();
    }
}
