package io.evenly.core.shared.config;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.util.Collections;
import java.util.List;

/**
 * Provider for DotEnvConfigSource.
 * This is registered via META-INF/services/org.eclipse.microprofile.config.spi.ConfigSourceProvider
 */
public class DotEnvConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return List.of(new DotEnvConfigSource());
    }
}
