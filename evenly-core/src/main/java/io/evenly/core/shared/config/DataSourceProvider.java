package io.evenly.core.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

@ApplicationScoped
public class DataSourceProvider {

    @Inject
    @ConfigProperty(name = "db.url", defaultValue = "jdbc:postgresql://localhost:5432/evenly")
    private String dbUrl;

    @Inject
    @ConfigProperty(name = "db.user", defaultValue = "evenly")
    private String dbUser;

    @Inject
    @ConfigProperty(name = "db.password", defaultValue = "evenly")
    private String dbPassword;

    private HikariDataSource dataSource;
    private volatile boolean migrationsRun = false;

    @PostConstruct
    void init() {
        var config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setPoolName("evenly-pool");
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Observer method that runs Flyway migrations when the ApplicationScoped context is initialized.
     * This ensures migrations run early in the application lifecycle, before any database operations.
     * The observer runs after @PostConstruct, so dataSource is guaranteed to be initialized.
     */
    void runMigrations(@Observes @Initialized(ApplicationScoped.class) Object init) {
        runMigrationsIfNeeded();
    }

    @PreDestroy
    void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Produces
    @Singleton
    public DataSource dataSource() {
        // Ensure migrations run if they haven't already (fallback safety)
        if (!migrationsRun && this.dataSource != null) {
            runMigrationsIfNeeded();
        }
        return this.dataSource;
    }

    /**
     * Internal method to run migrations. Called by observer or as fallback.
     */
    private synchronized void runMigrationsIfNeeded() {
        if (!migrationsRun && dataSource != null) {
            Flyway flyway = Flyway.configure()
                    .dataSource(this.dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            
            try {
                flyway.migrate();
                System.out.println("✅ Flyway migrations completed successfully");
                migrationsRun = true;
            } catch (Exception e) {
                System.err.println("❌ Flyway migration failed: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database migration failed", e);
            }
        }
    }
}
