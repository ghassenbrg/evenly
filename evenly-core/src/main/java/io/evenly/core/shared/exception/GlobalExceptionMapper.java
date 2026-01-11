package io.evenly.core.shared.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception mapper that catches all unhandled exceptions and returns
 * proper HTTP error responses instead of letting them propagate to the default mapper.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {

        // Determine status code based on exception type
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String errorMessage = "An unexpected error occurred";

        if (exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST;
            errorMessage = exception.getMessage();
        } else if (exception instanceof SecurityException) {
            status = Response.Status.FORBIDDEN;
            errorMessage = exception.getMessage();
        } else if (exception instanceof NotFoundException) {
            // JAX-RS NotFoundException (e.g., when resource method doesn't match)
            status = Response.Status.NOT_FOUND;
            errorMessage = exception.getMessage() != null ? exception.getMessage() : "Resource not found";
        } else if (exception instanceof io.evenly.core.shared.exception.NotFoundException) {
            // Custom NotFoundException
            status = Response.Status.NOT_FOUND;
            errorMessage = exception.getMessage();
        } else if (exception instanceof io.evenly.core.shared.exception.ConflictException) {
            status = Response.Status.CONFLICT;
            errorMessage = exception.getMessage();
        } else if (exception instanceof RuntimeException) {
            // Check if it's a known runtime exception with a specific message
            String message = exception.getMessage();
            if (message != null) {
                if (message.contains("Authentication") || message.contains("JWT") || message.contains("token")) {
                    status = Response.Status.UNAUTHORIZED;
                    errorMessage = message;
                } else if (message.contains("not found") || message.contains("NotFound")) {
                    status = Response.Status.NOT_FOUND;
                    errorMessage = message;
                } else {
                    errorMessage = message;
                }
            }
        }

        // Log the exception at appropriate level
        if (status.getStatusCode() >= 500) {
            // Server errors - log with full stack trace
            logger.error("Internal server error ({}): {}", status.getStatusCode(), errorMessage, exception);
        } else if (status.getStatusCode() == 404) {
            // Not found - log at debug level to avoid spam
            logger.debug("Resource not found: {}", errorMessage);
        } else if (status.getStatusCode() == 401 || status.getStatusCode() == 403) {
            // Authentication/authorization errors - log at info level
            logger.info("Authentication/authorization error ({}): {}", status.getStatusCode(), errorMessage);
        } else {
            // Other client errors - log at warn level
            logger.warn("Client error ({}): {}", status.getStatusCode(), errorMessage);
        }

        // Build error response - use HashMap instead of Map.of() for proper serialization
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", errorMessage != null ? errorMessage : "An unexpected error occurred");
        errorResponse.put("status", status.getStatusCode());

        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorResponse)
            .build();
    }
}
