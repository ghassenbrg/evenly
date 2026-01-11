package io.evenly.core.shared.security;

import java.util.Optional;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

/**
 * Provides access to the current user's security context from JWT claims.
 * This can be injected into resources to access user information.
 */
@RequestScoped
public class SecurityContextProvider {

    @Context
    private ContainerRequestContext requestContext;

    /**
     * Get the current user's ID from the JWT token.
     */
    public Optional<String> getUserId() {
        if (requestContext == null) {
            return Optional.empty();
        }
        Object userId = requestContext.getProperty("user.id");
        return userId != null ? Optional.of(userId.toString()) : Optional.empty();
    }

    /**
     * Get the current user's username from the JWT token.
     */
    public Optional<String> getUsername() {
        if (requestContext == null) {
            return Optional.empty();
        }
        Object username = requestContext.getProperty("user.username");
        return username != null ? Optional.of(username.toString()) : Optional.empty();
    }

    /**
     * Get the current user's email from the JWT token.
     */
    public Optional<String> getEmail() {
        if (requestContext == null) {
            return Optional.empty();
        }
        Object email = requestContext.getProperty("user.email");
        return email != null ? Optional.of(email.toString()) : Optional.empty();
    }

    /**
     * Get the full JWT claims set.
     */
    public Optional<JWTClaimsSet> getClaims() {
        if (requestContext == null) {
            return Optional.empty();
        }
        Object claims = requestContext.getProperty("jwt.claims");
        return claims instanceof JWTClaimsSet ? Optional.of((JWTClaimsSet) claims) : Optional.empty();
    }

    /**
     * Check if the current request is authenticated.
     */
    public boolean isAuthenticated() {
        return getUserId().isPresent();
    }
}
