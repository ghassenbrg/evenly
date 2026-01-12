package io.evenly.core.mock.config;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * CDI Extension to conditionally veto implementations based on mock profile.
 * - Vetoes mock implementations when NOT in mock profile
 * - Vetoes JPA repository implementations when IN mock profile (to prevent null EntityManager errors)
 */
public class MockProfileExtension implements Extension {
    
    private static final boolean MOCK_PROFILE_ACTIVE = MockProfileActivator.isMockProfileActive();
    
    /**
     * Conditionally vetoes classes based on mock profile state.
     */
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        Class<T> javaClass = pat.getAnnotatedType().getJavaClass();
        String className = javaClass.getName();
        
        // Veto mock classes when mock profile is NOT active
        if (className.startsWith("io.evenly.core.mock.")) {
            if (!MOCK_PROFILE_ACTIVE) {
                pat.veto();
            }
        }
        
        // Veto JPA repository implementations when mock profile IS active
        // This prevents null EntityManager errors when running in mock mode
        if (MOCK_PROFILE_ACTIVE) {
            if (className.contains(".persistence.") && 
                className.endsWith("RepositoryImpl") &&
                !className.startsWith("io.evenly.core.mock.")) {
                pat.veto();
            }
        }
    }
}
