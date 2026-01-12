package io.evenly.core.mock.config;

import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Default;

/**
 * Producer for MockDataProvider that only creates the bean when mock profile is active.
 * This ensures MockDataProvider is not instantiated in production runs.
 */
@ApplicationScoped
public class MockDataProviderProducer {
    
    @Produces
    @ApplicationScoped
    @Default
    public MockDataProvider produceMockDataProvider() {
        if (MockProfileActivator.isMockProfileActive()) {
            MockDataProvider provider = new MockDataProvider();
            provider.init();
            return provider;
        }
        // Return null when not in mock profile - CDI will not inject this
        return null;
    }
}
