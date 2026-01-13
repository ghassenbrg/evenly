package io.evenly.core.features.analytics.impl;

import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.features.analytics.AnalyticsService;
import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotItem;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotResponse;
import io.evenly.core.features.analytics.dto.ExpenseSummary;
import io.evenly.core.features.analytics.dto.LinearChartDataPoint;
import io.evenly.core.features.balance.BalanceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnalyticsServiceImpl implements AnalyticsService {

    @Inject
    private BalanceService balanceService;

    @Inject
    private ExpenseRepository expenseRepository;

    @Override
    public BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate) {
        // Delegate to BalanceService
        return balanceService.getBalanceSummary(workspaceId, userId, startDate, endDate);
    }

    @Override
    public ExpenseSnapshotResponse getExpensesSnapshot(String workspaceId, LocalDate startDate, LocalDate endDate, int size) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid, startDate, endDate, null, 0, Integer.MAX_VALUE, null);
        
        // Group by category and sum amounts
        Map<UUID, BigDecimal> categoryTotals = new HashMap<>();
        for (io.evenly.core.domain.Expense expense : expenses) {
            if (expense.getCategoryId() != null) {
                categoryTotals.merge(expense.getCategoryId(), expense.getAmount(), BigDecimal::add);
            }
        }
        
        // Convert to ExpenseSnapshotItem and sort by amount descending
        List<ExpenseSnapshotItem> items = categoryTotals.entrySet().stream()
            .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
            .limit(size)
            .map(entry -> {
                ExpenseSnapshotItem item = new ExpenseSnapshotItem();
                item.setCategoryId(entry.getKey().toString());
                item.setTotalAmount(entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
        
        ExpenseSnapshotResponse response = new ExpenseSnapshotResponse();
        response.setData(items);
        response.setCategoriesCount(items.size());
        response.setRemainingCategoriesCount(Math.max(0, categoryTotals.size() - size));
        
        return response;
    }

    @Override
    public ExpenseSummary getExpensesSummary(String workspaceId, LocalDate startDate, LocalDate endDate) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid, startDate, endDate, null, 0, Integer.MAX_VALUE, null);
        
        if (expenses.isEmpty()) {
            ExpenseSummary summary = new ExpenseSummary();
            summary.setTotalAmount(BigDecimal.ZERO);
            summary.setExpensesCount(0);
            summary.setAveragePerDay(BigDecimal.ZERO);
            summary.setLargestExpenseAmount(BigDecimal.ZERO);
            summary.setLinearChartData(List.of());
            // Get currency from workspace
            return summary;
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (io.evenly.core.domain.Expense expense : expenses) {
            totalAmount = totalAmount.add(expense.getAmount());
        }
        
        int expensesCount = expenses.size();
        
        // Calculate days in range
        long days = startDate != null && endDate != null 
            ? java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
            : 1;
        
        BigDecimal averagePerDay = totalAmount.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        
        BigDecimal largestExpenseAmount = BigDecimal.ZERO;
        for (io.evenly.core.domain.Expense expense : expenses) {
            if (expense.getAmount().compareTo(largestExpenseAmount) > 0) {
                largestExpenseAmount = expense.getAmount();
            }
        }
        
        // Get currency from first expense
        String currency = expenses.get(0).getCurrency();
        
        // Generate linear chart data (group by date)
        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        for (io.evenly.core.domain.Expense expense : expenses) {
            dailyTotals.merge(expense.getEffectiveDate(), expense.getAmount(), BigDecimal::add);
        }
        
        List<LinearChartDataPoint> linearChartData = dailyTotals.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                LinearChartDataPoint point = new LinearChartDataPoint();
                point.setDate(entry.getKey().toString());
                point.setAmount(entry.getValue());
                return point;
            })
            .collect(Collectors.toList());
        
        ExpenseSummary summary = new ExpenseSummary();
        summary.setTotalAmount(totalAmount);
        summary.setExpensesCount(expensesCount);
        summary.setAveragePerDay(averagePerDay);
        summary.setCurrency(currency);
        summary.setLargestExpenseAmount(largestExpenseAmount);
        summary.setLinearChartData(linearChartData);
        
        return summary;
    }
}
