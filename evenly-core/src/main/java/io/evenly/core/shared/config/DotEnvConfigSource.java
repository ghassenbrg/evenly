package io.evenly.core.shared.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ConfigSource that reads environment variables from .env file using dotenv-java.
 * This allows using .env files for local development.
 * 
 * Priority: 250 (higher than default application.properties)
 * Environment variables from system still take precedence (priority 300).
 */
public class DotEnvConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(DotEnvConfigSource.class);
    private static final String NAME = "DotEnvConfigSource";
    private static final int ORDINAL = 250;
    
    private final Map<String, String> properties;

    public DotEnvConfigSource() {
        this.properties = new HashMap<>();
        Dotenv env = null;
        
        try {
            // Try multiple paths for .env file
            String[] paths = {
                "./",                    // Current working directory
                "./evenly-core/",        // If running from project root
                ".",                     // Current directory (alternative)
                System.getProperty("user.dir") + "/",  // Explicit working directory
            };
            
            Dotenv loadedEnv = null;
            for (String path : paths) {
                try {
                    loadedEnv = Dotenv.configure()
                        .directory(path)
                        .ignoreIfMissing()
                        .load();
                    if (loadedEnv != null && !loadedEnv.entries().isEmpty()) {
                        env = loadedEnv;
                        logger.debug("Loaded .env file from: {}", path);
                        break;
                    }
                } catch (Exception e) {
                    // Try next path
                    continue;
                }
            }
            
            // If still not found, try without directory (uses default lookup)
            if (env == null) {
                try {
                    env = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                    if (env != null && !env.entries().isEmpty()) {
                        logger.debug("Loaded .env file from default location");
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            if (env != null && !env.entries().isEmpty()) {
                // Load all entries from .env file
                env.entries().forEach(entry -> {
                    properties.put(entry.getKey(), entry.getValue());
                });
                
                logger.info("Loaded {} properties from .env file", properties.size());
            } else {
                logger.debug("No .env file found in: {}", String.join(", ", paths));
            }
        } catch (Exception e) {
            logger.warn("Failed to load .env file: {}", e.getMessage());
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        // Return value from .env file
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }
}
