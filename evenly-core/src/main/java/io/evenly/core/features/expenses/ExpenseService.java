package io.evenly.core.features.expenses;

import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.expenses.dto.CreateExpenseRequest;
import io.evenly.core.features.expenses.dto.UpdateExpenseRequest;
import io.evenly.core.shared.common.PaginatedExpenses;
import io.evenly.core.shared.common.SettlementScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for expense operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface ExpenseService {
    PaginatedExpenses findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
                                       String categoryId, SettlementScope settlementScope, int page, int size, String sort);
    List<Expense> findRecentForWorkspace(String workspaceId, int size);
    Optional<Expense> findById(String expenseId);
    Expense create(String workspaceId, String userId, CreateExpenseRequest request);
    Expense update(String expenseId, String userId, UpdateExpenseRequest request);
    void delete(String expenseId, String userId);
}
