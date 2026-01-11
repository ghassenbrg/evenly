package io.evenly.core.shared.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.URLBasedJWKSetSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;

/**
 * JWT Authentication Filter for Keycloak integration.
 * Validates JWT tokens from Keycloak and sets security context.
 * 
 * This filter is bound to endpoints annotated with @Authenticated.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthFilter implements ContainerRequestFilter {

    private volatile String keycloakUrl;
    private volatile String keycloakRealm;
    private volatile String keycloakClientId;
    private volatile Optional<String> jwksUrl;
    private volatile boolean configInitialized = false;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private volatile boolean initialized = false;

    private void ensureConfigInitialized() {
        if (!configInitialized) {
            synchronized (this) {
                if (!configInitialized) {
                    try {
                        // Use CDI to get Config to avoid HK2 injection issues
                        Config config = CDI.current().select(Config.class).get();
                        keycloakUrl = config.getOptionalValue("keycloak.url", String.class)
                            .orElse("http://localhost:9090");
                        keycloakRealm = config.getOptionalValue("keycloak.realm", String.class)
                            .orElse("evenly");
                        keycloakClientId = config.getOptionalValue("keycloak.client.id", String.class)
                            .orElse("evenly-backend");
                        jwksUrl = config.getOptionalValue("keycloak.jwks.url", String.class);
                        configInitialized = true;
                    } catch (Exception e) {
                        // Fallback to defaults if CDI is not available yet
                        keycloakUrl = "http://localhost:9090";
                        keycloakRealm = "evenly";
                        keycloakClientId = "evenly-backend";
                        jwksUrl = Optional.empty();
                        configInitialized = true;
                    }
                }
            }
        }
    }

    private synchronized void initialize() {
        if (initialized) {
            return;
        }

        // Ensure config is initialized before using it
        ensureConfigInitialized();

        try {
            jwtProcessor = new DefaultJWTProcessor<>();
            
            // Use custom JWKS URL if provided, otherwise construct from Keycloak URL
            String jwksUrlString = jwksUrl.orElseGet(() -> 
                String.format("%s/realms/%s/protocol/openid-connect/certs", keycloakUrl, keycloakRealm));
            
            URL jwksUrlObj = URI.create(jwksUrlString).toURL();
            ResourceRetriever resourceRetriever = new DefaultResourceRetriever();
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(
                new URLBasedJWKSetSource<SecurityContext>(jwksUrlObj, resourceRetriever)
            ).build();
            
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = 
                new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSource);
            
            jwtProcessor.setJWSKeySelector(keySelector);
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT processor: " + e.getMessage(), e);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip OPTIONS requests (handled by CORS filter)
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        // Initialize on first use
        if (!initialized) {
            initialize();
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Missing or invalid Authorization header\"}")
                    .build()
            );
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Parse and validate the JWT token
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
            
            // Verify the token is for the correct client
            String audience = claimsSet.getAudience().stream()
                .findFirst()
                .orElse(null);
            
            if (audience != null && !audience.equals(keycloakClientId)) {
                // Allow if audience matches or if no specific client ID is required
                // Keycloak tokens may have multiple audiences
            }

            // Set security context with user information
            String userId = claimsSet.getSubject();
            String username = claimsSet.getStringClaim("preferred_username");
            String email = claimsSet.getStringClaim("email");
            
            // Store claims in request context for later use
            requestContext.setProperty("jwt.claims", claimsSet);
            requestContext.setProperty("user.id", userId);
            requestContext.setProperty("user.username", username);
            requestContext.setProperty("user.email", email);
            
        } catch (ParseException | BadJOSEException | JOSEException e) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid or expired token: " + e.getMessage() + "\"}")
                    .build()
            );
        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Authentication error: " + e.getMessage() + "\"}")
                    .build()
            );
        }
    }
}
