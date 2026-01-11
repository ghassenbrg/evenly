package io.evenly.core.features.analytics;

import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotResponse;
import io.evenly.core.features.analytics.dto.ExpenseSummary;
import java.time.LocalDate;

/**
 * Service interface for analytics operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface AnalyticsService {
    BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate);
    ExpenseSnapshotResponse getExpensesSnapshot(String workspaceId, LocalDate startDate, LocalDate endDate, int size);
    ExpenseSummary getExpensesSummary(String workspaceId, LocalDate startDate, LocalDate endDate);
}
