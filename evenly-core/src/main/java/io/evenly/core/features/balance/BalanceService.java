package io.evenly.core.features.balance;

import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.balance.dto.SettleUpResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for balance and settlement operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface BalanceService {
    List<Balance> getBalanceForWorkspace(String workspaceId);
    SettleUpResponse getSettleUpForWorkspace(String workspaceId, String currentUserId);
    BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate);
}
