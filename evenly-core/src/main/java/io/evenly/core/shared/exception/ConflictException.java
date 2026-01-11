package io.evenly.core.shared.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., cannot delete).
 * Maps to 409 HTTP status code.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
