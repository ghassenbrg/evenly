package io.evenly.core.features.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.evenly.core.features.auth.dto.AuthResponse;
import io.evenly.core.features.auth.dto.RegisterRequest;
import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.workspaces.WorkspaceService;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * User authentication and profile endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserResource {

    @Inject
    private SecurityContextProvider securityContext;

    @Inject
    private UserService userService;

    @Inject
    private WorkspaceService workspaceService;

    /**
     * Register a new user.
     * This endpoint does not require authentication.
     */
    @POST
    @Path("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Valid RegisterRequest request) {
        // Generate a user ID (in real implementation, this would come from Keycloak)
        String userId = request.getUsername(); // Use username as ID for mock

        // Check if user already exists
        if (userService.findById(userId).isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "USER_ALREADY_EXISTS");
            errorResponse.put("message", "User with this username already exists");
            errorResponse.put("details", new HashMap<>());
            errorResponse.put("traceId", UUID.randomUUID().toString());
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        }

        // In a real implementation, we would:
        // 1. Create user in Keycloak
        // 2. Get JWT token from Keycloak
        // 3. Store user in database

        // For mock implementation, we'll create a simple token
        // In production, this would be a real JWT from Keycloak
        String mockToken = "mock-jwt-token-" + userId;

        // Create user using service
        User user = userService.getOrCreate(userId, request.getEmail(), request.getUsername());
        user.setDisplayName(request.getDisplayName());
        String preferredCurrency = request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "USD";
        user.setPreferredCurrency(preferredCurrency);

        // Create personal workspace for the new user
        workspaceService.createPersonalWorkspace(userId, preferredCurrency);

        AuthResponse authResponse = new AuthResponse(mockToken, user);
        return Response.ok(authResponse).build();
    }

    /**
     * Get current authenticated user.
     * This endpoint requires authentication.
     */
    @GET
    @Path("/user/me")
    @Authenticated
    public Response getCurrentUser() {
        return securityContext.getUserId()
                .map(userId -> {
                    User user = userService.getOrCreate(
                            userId,
                            securityContext.getEmail().orElse(userId + "@example.com"),
                            securityContext.getUsername().orElse(userId));
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
