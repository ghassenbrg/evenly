package io.evenly.core.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Activates CDI request context for all requests.
 * 
 * This is needed because @PreMatching filters run BEFORE Helidon activates
 * the CDI request context. Without this filter, @RequestScoped beans won't work.
 * 
 * Other Helidon apps that use @RequestScoped successfully either:
 * 1. Don't use @PreMatching filters, OR
 * 2. Have a similar filter that activates the context early
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 2) // Run before CorsFilter and AuthFilter
@ApplicationScoped
public class CdiRequestContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_CONTEXT_KEY = "cdi.request.context.controller";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Activate CDI request context for all requests
        try {
            RequestContextController controller = CDI.current().select(RequestContextController.class).get();
            if (controller != null) {
                boolean activated = controller.activate();
                // Store the controller so we can deactivate it later
                requestContext.setProperty(REQUEST_CONTEXT_KEY, activated ? controller : null);
            }
        } catch (Exception e) {
            // If context activation fails, log but don't fail the request
            System.err.println("Failed to activate CDI request context: " + e.getMessage());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        // Deactivate CDI request context if we activated it
        try {
            Object controllerObj = requestContext.getProperty(REQUEST_CONTEXT_KEY);
            if (controllerObj instanceof RequestContextController controller && controller != null) {
                controller.deactivate();
            }
        } catch (Exception e) {
            // If context deactivation fails, log but don't fail the response
            System.err.println("Failed to deactivate CDI request context: " + e.getMessage());
        }
    }
}

