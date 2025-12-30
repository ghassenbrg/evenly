package io.evenly.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Map;

@ApplicationScoped
public class DataSourceProvider {

    private HikariDataSource dataSource;

    @PostConstruct
    void init() {
        var config = new HikariConfig();
        Map<String, String> env = System.getenv();
        config.setJdbcUrl(env.getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/evenly"));
        config.setUsername(env.getOrDefault("DB_USER", "evenly"));
        config.setPassword(env.getOrDefault("DB_PASSWORD", "evenly"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setPoolName("evenly-pool");
        this.dataSource = new HikariDataSource(config);
        
        // Flyway migrations - will run when migrations are added
        Flyway.configure()
                .dataSource(this.dataSource)
                .load()
                .migrate();
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
        return this.dataSource;
    }
}
