package io.evenly.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a currency (reference data).
 * Maps to the 'currencies' table in the database.
 */
@Entity
@Table(name = "currencies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    @Id
    @Column(name = "code", length = 3)
    private String code; // ISO 4217 currency code (e.g., 'JPY', 'USD')
    
    @Column(name = "name", nullable = false, length = 255)
    private String name; // Currency display name (e.g., 'Japanese Yen')
    
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol; // Currency symbol (e.g., '¥', '$')
}
