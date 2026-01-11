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
import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;

/**
 * JWT Authentication Filter for Keycloak integration.
 * Validates JWT tokens from Keycloak and sets security context.
 * 
 * This filter is bound to endpoints annotated with @Authenticated via JwtAuthFeature.
 * Note: This filter does NOT have @Provider annotation to prevent it from applying globally.
 */
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

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
                        
                        // Treat empty strings as missing (use defaults)
                        Optional<String> keycloakUrlOpt = config.getOptionalValue("keycloak.url", String.class);
                        Optional<String> realmOpt = config.getOptionalValue("keycloak.realm", String.class);
                        Optional<String> clientIdOpt = config.getOptionalValue("keycloak.client.id", String.class);
                        
                        keycloakUrl = keycloakUrlOpt
                            .filter(url -> url != null && !url.trim().isEmpty())
                            .orElse("http://localhost:9090");
                        keycloakRealm = realmOpt
                            .filter(realm -> realm != null && !realm.trim().isEmpty())
                            .orElse("evenly");
                        keycloakClientId = clientIdOpt
                            .filter(id -> id != null && !id.trim().isEmpty())
                            .orElse("evenly-backend");
                        jwksUrl = config.getOptionalValue("keycloak.jwks.url", String.class)
                            .filter(url -> url != null && !url.trim().isEmpty());
                        
                        logger.debug("Keycloak configuration loaded - URL: {}, Realm: {}, Client ID: {}", 
                            keycloakUrl, keycloakRealm, keycloakClientId);
                        
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
            
            URI uri = URI.create(jwksUrlString);
            
            if (!uri.isAbsolute()) {
                throw new IllegalStateException(
                    String.format("JWKS URL is not absolute: '%s'. Keycloak URL: '%s', Realm: '%s'. " +
                                "Please ensure keycloak.url includes protocol (http:// or https://)",
                                jwksUrlString, keycloakUrl, keycloakRealm));
            }
            
            logger.debug("Initializing JWT processor with JWKS URL: {}", jwksUrlString);
            
            URL jwksUrlObj = uri.toURL();
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
            logger.debug("Missing or invalid Authorization header for {}", requestContext.getUriInfo().getRequestUri());
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
                logger.debug("Token audience mismatch: expected {}, got {}", keycloakClientId, audience);
            }

            // Set security context with user information
            // Use preferred_username as user ID (subject may be null in some Keycloak tokens)
            String username = claimsSet.getStringClaim("preferred_username");
            String userId = claimsSet.getSubject();
            // If subject is null, use preferred_username as the user ID
            if (userId == null || userId.trim().isEmpty()) {
                userId = username;
            }
            String email = claimsSet.getStringClaim("email");
            
            // Store claims in request context for later use
            requestContext.setProperty("jwt.claims", claimsSet);
            requestContext.setProperty("user.id", userId);
            requestContext.setProperty("user.username", username);
            requestContext.setProperty("user.email", email);
            
        } catch (ParseException | BadJOSEException | JOSEException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid or expired token: " + e.getMessage() + "\"}")
                    .build()
            );
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Authentication error: " + e.getMessage() + "\"}")
                    .build()
            );
        }
    }
}
