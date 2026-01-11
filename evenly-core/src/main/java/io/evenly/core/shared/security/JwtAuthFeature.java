package io.evenly.core.shared.security;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic feature that binds JwtAuthFilter to endpoints annotated with @Authenticated.
 */
@Provider
public class JwtAuthFeature implements DynamicFeature {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFeature.class);

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        // Check if the resource class or method is annotated with @Authenticated
        boolean isAuthenticated = resourceInfo.getResourceMethod().isAnnotationPresent(Authenticated.class)
                || resourceInfo.getResourceClass().isAnnotationPresent(Authenticated.class);

        if (isAuthenticated) {
            context.register(JwtAuthFilter.class);
            logger.debug("JwtAuthFilter registered for {}.{}", 
                resourceInfo.getResourceClass().getName(), 
                resourceInfo.getResourceMethod().getName());
        }
    }
}
