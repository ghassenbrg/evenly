package io.evenly.core.infrastructure.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.cfg.AvailableSettings;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * CDI producer for EntityManagerFactory configured for JTA transactions.
 * Creates a singleton EMF that is shared across the application.
 * Uses Narayana JTA for transaction management.
 */
@ApplicationScoped
public class EntityManagerFactoryProducer {

    @Inject
    private DataSource dataSource;

    @Inject
    @ConfigProperty(name = "hibernate.show_sql", defaultValue = "false")
    private boolean showSql;

    @Inject
    @ConfigProperty(name = "hibernate.format_sql", defaultValue = "false")
    private boolean formatSql;

    @Inject
    @ConfigProperty(name = "hibernate.jdbc.batch_size", defaultValue = "20")
    private int batchSize;

    @Inject
    @ConfigProperty(name = "hibernate.order_inserts", defaultValue = "true")
    private boolean orderInserts;

    @Inject
    @ConfigProperty(name = "hibernate.order_updates", defaultValue = "true")
    private boolean orderUpdates;

    @Inject
    @ConfigProperty(name = "hibernate.jdbc.time_zone", defaultValue = "UTC")
    private String timeZone;

    private EntityManagerFactory emf;

    @PostConstruct
    void init() {
        Map<String, Object> properties = new HashMap<>();
        
        // Use JTA DataSource (required for JTA mode)
        properties.put("jakarta.persistence.jtaDataSource", dataSource);
        
        // Hibernate dialect
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        
        // Transaction coordinator - use JTA
        properties.put(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jta");
        
        // JTA Platform - Narayana (use short name for Hibernate 6.4)
        properties.put(AvailableSettings.JTA_PLATFORM, "JBossTS");
        
        // Connection handling for JTA
        properties.put(AvailableSettings.CONNECTION_HANDLING, "delayed_acquisition_and_release_after_transaction");
        
        // SQL logging (disabled by default, enable via config for dev)
        properties.put(AvailableSettings.SHOW_SQL, showSql);
        properties.put(AvailableSettings.FORMAT_SQL, formatSql);
        
        // Schema generation (disabled - using Flyway)
        properties.put(AvailableSettings.HBM2DDL_AUTO, "none");
        
        // Performance optimizations
        properties.put("hibernate.jdbc.batch_size", batchSize);
        properties.put(AvailableSettings.ORDER_INSERTS, orderInserts);
        properties.put(AvailableSettings.ORDER_UPDATES, orderUpdates);
        
        // Timezone handling
        properties.put(AvailableSettings.JDBC_TIME_ZONE, timeZone);
        
        emf = Persistence.createEntityManagerFactory("evenly", properties);
    }

    @Produces
    @ApplicationScoped
    public EntityManagerFactory entityManagerFactory() {
        return emf;
    }

    @PreDestroy
    void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
