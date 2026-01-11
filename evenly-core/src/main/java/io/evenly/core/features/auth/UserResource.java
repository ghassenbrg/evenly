package io.evenly.core.features.auth;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.auth.UserService;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
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

    @Inject
    private UserService userService;

    @GET
    @Path("/me")
    public Response getCurrentUser() {
        return securityContext.getUserId()
            .map(userId -> {
                User user = userService.getOrCreate(
                    userId,
                    securityContext.getEmail().orElse(userId + "@example.com"),
                    securityContext.getUsername().orElse(userId)
                );
                return Response.ok(user).build();
            })
            .orElse(Response.status(Response.Status.UNAUTHORIZED)
                .entity(createErrorResponse("User not authenticated"))
                .build());
    }
    
    private Map<String, String> createErrorResponse(String error) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        return errorResponse;
    }
}
