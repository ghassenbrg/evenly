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
        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String errorMessage = "An unexpected error occurred";

        if (exception instanceof jakarta.validation.ConstraintViolationException) {
            statusCode = 422; // UNPROCESSABLE_ENTITY
            errorMessage = "Validation failed";
        } else if (exception instanceof IllegalArgumentException) {
            statusCode = Response.Status.BAD_REQUEST.getStatusCode();
            errorMessage = exception.getMessage();
        } else if (exception instanceof SecurityException) {
            statusCode = Response.Status.FORBIDDEN.getStatusCode();
            errorMessage = exception.getMessage();
        } else if (exception instanceof NotFoundException) {
            // JAX-RS NotFoundException (e.g., when resource method doesn't match)
            statusCode = Response.Status.NOT_FOUND.getStatusCode();
            errorMessage = exception.getMessage() != null ? exception.getMessage() : "Resource not found";
        } else if (exception instanceof io.evenly.core.shared.exception.NotFoundException) {
            // Custom NotFoundException
            statusCode = Response.Status.NOT_FOUND.getStatusCode();
            errorMessage = exception.getMessage();
        } else if (exception instanceof io.evenly.core.shared.exception.ConflictException) {
            statusCode = Response.Status.CONFLICT.getStatusCode();
            errorMessage = exception.getMessage();
        } else if (exception instanceof RuntimeException) {
            // Check if it's a known runtime exception with a specific message
            String message = exception.getMessage();
            if (message != null) {
                if (message.contains("Authentication") || message.contains("JWT") || message.contains("token")) {
                    statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                    errorMessage = message;
                } else if (message.contains("not found") || message.contains("NotFound")) {
                    statusCode = Response.Status.NOT_FOUND.getStatusCode();
                    errorMessage = message;
                } else {
                    errorMessage = message;
                }
            }
        }

        // Log the exception at appropriate level
        if (statusCode >= 500) {
            // Server errors - log with full stack trace
            logger.error("Internal server error ({}): {}", statusCode, errorMessage, exception);
        } else if (statusCode == 404) {
            // Not found - log at debug level to avoid spam
            logger.debug("Resource not found: {}", errorMessage);
        } else if (statusCode == 401 || statusCode == 403) {
            // Authentication/authorization errors - log at info level
            logger.info("Authentication/authorization error ({}): {}", statusCode, errorMessage);
        } else {
            // Other client errors - log at warn level
            logger.warn("Client error ({}): {}", statusCode, errorMessage);
        }

        // Generate trace ID for error tracking
        String traceId = java.util.UUID.randomUUID().toString();
        
        // Build error response with consistent format: errorCode, message, details, traceId
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", getErrorCode(statusCode));
        errorResponse.put("message", errorMessage != null ? errorMessage : "An unexpected error occurred");
        Map<String, Object> details = getErrorDetails(exception, statusCode);
        errorResponse.put("details", details);
        errorResponse.put("traceId", traceId);
        errorResponse.put("status", statusCode);
        
        // For backward compatibility with webapp, also include 'errors' at top level for validation errors
        if (details.containsKey("errors")) {
            errorResponse.put("errors", details.get("errors"));
        }

        return Response.status(statusCode)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorResponse)
            .build();
    }
    
    private String getErrorCode(int statusCode) {
        // Generate error code based on status
        return switch (statusCode) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> "VALIDATION_ERROR";
            case 500 -> "INTERNAL_SERVER_ERROR";
            default -> "ERROR_" + statusCode;
        };
    }
    
    private Map<String, Object> getErrorDetails(Throwable exception, int statusCode) {
        Map<String, Object> details = new HashMap<>();
        
        // Add validation errors if available (from Bean Validation)
        if (exception instanceof jakarta.validation.ConstraintViolationException) {
            jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) exception;
            java.util.List<Map<String, String>> errors = new java.util.ArrayList<>();
            cve.getConstraintViolations().forEach(violation -> {
                Map<String, String> error = new HashMap<>();
                error.put("field", violation.getPropertyPath().toString());
                error.put("message", violation.getMessage());
                errors.add(error);
            });
            details.put("errors", errors);
        }
        
        return details;
    }
}
