package io.evenly.core.features.auth;

import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * User authentication and profile endpoints.
 * 
 * This endpoint requires authentication - clients must include a valid
 * Keycloak JWT token in the Authorization header: "Bearer <token>"
 */
@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class UserResource {

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    @Path("/me")
    public Response getCurrentUser() {
        return securityContext.getUserId()
            .map(userId -> {
                Map<String, Object> userInfo = Map.of(
                    "id", userId,
                    "username", securityContext.getUsername().orElse("unknown"),
                    "email", securityContext.getEmail().orElse("unknown")
                );
                return Response.ok(userInfo).build();
            })
            .orElse(Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "User not authenticated"))
                .build());
    }
}
