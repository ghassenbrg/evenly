package io.evenly.core.mock.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Utility class to check if the mock profile is active.
 * The mock profile is enabled when:
 * - System property "mp.config.profile" is set to "mock"
 * - Environment variable "MP_CONFIG_PROFILE" is set to "mock"
 * - Config property "mp.config.profile" is set to "mock"
 */
public class MockProfileActivator {
    
    private static final String PROFILE_PROPERTY = "mp.config.profile";
    private static final String MOCK_PROFILE = "mock";
    
    /**
     * Checks if the mock profile is currently active.
     * 
     * @return true if mock profile is active, false otherwise
     */
    public static boolean isMockProfileActive() {
        try {
            Config config = ConfigProvider.getConfig();
            String activeProfile = config.getOptionalValue(PROFILE_PROPERTY, String.class)
                .orElse("");
            return MOCK_PROFILE.equalsIgnoreCase(activeProfile);
        } catch (Exception e) {
            // If config is not available, default to false (production mode)
            return false;
        }
    }
}
