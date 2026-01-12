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
import io.evenly.core.features.invites.dto.Invite;
import io.evenly.core.features.invites.dto.CreateInviteRequest;

/**
 * Workspace invitation endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class InviteResource {

    @Inject
    private InviteService inviteService;

    @Inject
    private SecurityContextProvider securityContext;

    @POST
    @Path("/workspaces/{workspaceId}/invites")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createInvite(@PathParam("workspaceId") String workspaceId,
                                 @Valid CreateInviteRequest request) {
        Invite invite = inviteService.create(workspaceId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", invite);
        return Response.ok(response).build();
    }
}
