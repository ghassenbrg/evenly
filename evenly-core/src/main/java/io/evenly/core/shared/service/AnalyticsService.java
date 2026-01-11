package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.BalanceSummary;
import io.evenly.core.shared.dto.ExpenseSnapshotResponse;

import java.time.LocalDate;

/**
 * Service interface for analytics operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface AnalyticsService {
    BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate);
    ExpenseSnapshotResponse getExpensesSnapshot(String workspaceId, LocalDate startDate, LocalDate endDate, int size);
}
