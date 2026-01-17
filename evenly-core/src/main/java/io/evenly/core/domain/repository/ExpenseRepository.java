package io.evenly.core.domain.repository;

import io.evenly.core.domain.Expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.evenly.core.shared.common.SettlementScope;

/**
 * Repository interface for Expense domain entities.
 * Port in the ports & adapters architecture.
 */
public interface ExpenseRepository {
    Optional<Expense> findById(UUID id);
    List<Expense> findByWorkspaceId(UUID workspaceId);
    List<Expense> findByWorkspaceId(UUID workspaceId, LocalDate startDate, LocalDate endDate,
                                    UUID categoryId, SettlementScope settlementScope, int page, int size, String sort);
    List<Expense> findByPaidByUserId(String userId); // userId is now String (username)
    List<Expense> findByCategoryId(UUID categoryId);
    List<Expense> findBySettlementId(UUID settlementId);
    Expense save(Expense expense);
    void delete(UUID id);
    boolean existsById(UUID id);
    long countByWorkspaceId(UUID workspaceId);
    long countByWorkspaceIdAndDateRange(UUID workspaceId, LocalDate startDate, LocalDate endDate,
                                        SettlementScope settlementScope);
}
