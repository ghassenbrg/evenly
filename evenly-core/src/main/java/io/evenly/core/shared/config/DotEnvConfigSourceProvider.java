package io.evenly.core.shared.config;

import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

/**
 * Provider for DotEnvConfigSource.
 * This is registered via
 * META-INF/services/org.eclipse.microprofile.config.spi.ConfigSourceProvider
 */
public class DotEnvConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return List.of(new DotEnvConfigSource());
    }
}
