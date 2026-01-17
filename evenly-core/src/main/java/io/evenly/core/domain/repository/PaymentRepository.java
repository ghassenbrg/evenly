package io.evenly.core.domain.repository;

import io.evenly.core.domain.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Payment domain entities.
 * Port in the ports & adapters architecture.
 */
public interface PaymentRepository {
    Optional<Payment> findById(UUID id);
    List<Payment> findByWorkspaceId(UUID workspaceId);
    List<Payment> findByWorkspaceId(UUID workspaceId, LocalDate startDate, LocalDate endDate,
                                     String status, Boolean settled, int page, int size, String sort);
    List<Payment> findByPayeeUserId(String userId); // userId is now String (username)
    List<Payment> findByPaidByUserId(String userId); // userId is now String (username)
    List<Payment> findBySettlementId(UUID settlementId);
    Payment save(Payment payment);
    void delete(UUID id);
    boolean existsById(UUID id);
    long countByWorkspaceId(UUID workspaceId, Boolean settled);
}
