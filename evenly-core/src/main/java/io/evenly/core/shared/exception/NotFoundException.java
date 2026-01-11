package io.evenly.core.shared.exception;

/**
 * Exception thrown when a resource is not found.
 * Maps to 404 HTTP status code.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
