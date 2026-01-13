package io.evenly.core.domain;

import io.evenly.core.features.currencies.SupportedCurrency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Domain entity representing a user account.
 * Maps to the 'users' table in the database.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id", nullable = false, unique = true, length = 100)
    private String id; // Username as primary key
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;
    
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    
    @Column(name = "preferred_currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private SupportedCurrency preferredCurrency;
    
    @Column(name = "locale", nullable = false, length = 10)
    @Builder.Default
    private String locale = "en-US";
    
    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "UTC";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
