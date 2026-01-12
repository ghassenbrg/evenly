package io.evenly.core.shared.security;

import java.text.ParseException;
import java.util.Optional;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the current user's security context from JWT claims.
 * This can be injected into resources to access user information.
 */
@RequestScoped
public class SecurityContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextProvider.class);

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
        
        // Try to get from JWT claims as fallback
        if (userId == null) {
            Object claimsObj = requestContext.getProperty("jwt.claims");
            if (claimsObj instanceof JWTClaimsSet) {
                try {
                    JWTClaimsSet claims = (JWTClaimsSet) claimsObj;
                    // Use preferred_username as userId (username is now the primary key)
                    String username = claims.getStringClaim("preferred_username");
                    if (username == null || username.trim().isEmpty()) {
                        username = claims.getSubject(); // Fallback to subject
                    }
                    userId = username;
                } catch (ParseException e) {
                    logger.debug("Error reading user ID from JWT claims", e);
                } catch (Exception e) {
                    logger.debug("Error reading user ID from JWT claims", e);
                }
            }
        }
        
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
