package io.evenly.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing a user account.
 * Maps to the 'users' table in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String email;
    private String displayName;
    private String username;
    private String avatarUrl;
    private String preferredCurrency;
    private String locale;
    private String timezone;
    private OffsetDateTime createdAt;
}
