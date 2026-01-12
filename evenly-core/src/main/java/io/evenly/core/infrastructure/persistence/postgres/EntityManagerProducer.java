package io.evenly.core.infrastructure.persistence.postgres;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.cfg.AvailableSettings;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Producer for EntityManager using RESOURCE_LOCAL transaction type.
 * Works with CDI DataSource from DataSourceProvider.
 */
@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private DataSource dataSource;

    private EntityManagerFactory emf;

    @PostConstruct
    void init() {
        Map<String, Object> properties = new HashMap<>();
        // Use the CDI-injected DataSource (non-JTA for RESOURCE_LOCAL)
        properties.put("jakarta.persistence.nonJtaDataSource", dataSource);
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(AvailableSettings.FORMAT_SQL, true);
        properties.put(AvailableSettings.SHOW_SQL, false);
        properties.put(AvailableSettings.HBM2DDL_AUTO, "none");
        
        emf = Persistence.createEntityManagerFactory("evenly", properties);
    }

    @Produces
    @ApplicationScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @PreDestroy
    void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
