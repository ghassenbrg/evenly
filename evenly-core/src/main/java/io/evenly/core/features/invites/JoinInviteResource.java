package io.evenly.core.features.invites;

import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import io.evenly.core.features.invites.dto.JoinInviteRequest;

/**
 * Join workspace via invite endpoint (authentication required).
 */
@Path("/api/invites")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class JoinInviteResource {

    @Inject
    private InviteService inviteService;

    @Inject
    private SecurityContextProvider securityContext;

    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinWorkspace(@Valid JoinInviteRequest request) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        inviteService.joinWorkspace(request.getCode(), userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully joined workspace");
        return Response.ok(response).build();
    }
}
