package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Expense;
import io.evenly.core.shared.dto.PaginatedExpenses;
import io.evenly.core.shared.dto.request.CreateExpenseRequest;
import io.evenly.core.shared.dto.request.UpdateExpenseRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for expense operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface ExpenseService {
    PaginatedExpenses findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
                                       String categoryId, String status, int page, int size, String sort);
    List<Expense> findRecentForWorkspace(String workspaceId, int size);
    Optional<Expense> findById(String expenseId);
    Expense create(String workspaceId, String userId, CreateExpenseRequest request);
    Expense update(String expenseId, UpdateExpenseRequest request);
    void delete(String expenseId);
}
