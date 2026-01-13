package io.evenly.core.infrastructure.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;

/**
 * MicroProfile Health Check for database connectivity.
 * Verifies that the application can connect to the database.
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    private DataSource dataSource;

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("database");

        try {
            // Test DataSource connection
            try (Connection conn = dataSource.getConnection()) {
                boolean isValid = conn.isValid(2); // 2 second timeout
                if (isValid) {
                    builder.up()
                            .withData("datasource", "available")
                            .withData("connection", "valid");
                } else {
                    builder.down()
                            .withData("datasource", "unavailable")
                            .withData("connection", "invalid");
                }
            }

            // Test EntityManagerFactory
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                builder.withData("entityManagerFactory", "open");
            } else {
                builder.down()
                        .withData("entityManagerFactory", "closed");
            }

        } catch (Exception e) {
            builder.down()
                    .withData("error", e.getMessage());
        }

        return builder.build();
    }
}
