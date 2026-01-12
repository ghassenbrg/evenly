package io.evenly.core.infrastructure.persistence.postgres;

import java.util.UUID;

/**
 * Utility class for UUID conversions.
 */
public class UuidHelper {
    public static UUID parseUuid(String id) {
        if (id == null) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id, e);
        }
    }
}
