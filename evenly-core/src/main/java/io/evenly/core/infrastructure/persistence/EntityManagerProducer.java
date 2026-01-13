package io.evenly.core.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * CDI producer for EntityManager in JTA mode.
 * 
 * In JTA mode, EntityManager lifecycle is managed by the container and tied to transactions.
 * The EntityManager is automatically enlisted in the current JTA transaction when used.
 * 
 * Using @RequestScoped ensures each HTTP request gets its own EntityManager instance,
 * which is automatically enlisted in the JTA transaction managed by @Transactional.
 */
@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private EntityManagerFactory emf;

    /**
     * Produces a request-scoped EntityManager.
     * The EntityManager is automatically enlisted in the current JTA transaction
     * when used within a @Transactional method.
     */
    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Disposes the EntityManager when the request scope ends.
     * In JTA mode, the EntityManager is automatically closed when the transaction completes,
     * but we ensure proper cleanup here as well.
     */
    public void closeEntityManager(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
