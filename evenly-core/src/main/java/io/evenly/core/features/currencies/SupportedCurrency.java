package io.evenly.core.features.currencies;

/**
 * Supported currencies enum.
 * Contains all currencies supported by the application.
 */
public enum SupportedCurrency {
    JPY("JPY", "Japanese Yen", "¥"),
    USD("USD", "US Dollar", "$"),
    TND("TND", "Tunisian Dinar", "DT"),
    EUR("EUR", "Euro", "€"),
    AUD("AUD", "Australian Dollar", "A$");

    private final String code;
    private final String name;
    private final String symbol;

    SupportedCurrency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Find a currency by its code.
     * @param code The currency code (e.g., "USD", "EUR")
     * @return The SupportedCurrency enum value, or null if not found
     */
    public static SupportedCurrency findByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SupportedCurrency currency : values()) {
            if (currency.code.equalsIgnoreCase(code)) {
                return currency;
            }
        }
        return null;
    }

    /**
     * Check if a currency code is supported.
     * @param code The currency code to check
     * @return true if the currency is supported, false otherwise
     */
    public static boolean isSupported(String code) {
        return findByCode(code) != null;
    }
}
