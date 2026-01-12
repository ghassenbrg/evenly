package io.evenly.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a currency (reference data).
 * Maps to the 'currencies' table in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    private String code; // ISO 4217 currency code (e.g., 'JPY', 'USD')
    private String name; // Currency display name (e.g., 'Japanese Yen')
    private String symbol; // Currency symbol (e.g., '¥', '$')
}
