package io.evenly.core.features.analytics.impl;

import io.evenly.core.domain.Category;
import io.evenly.core.domain.repository.CategoryRepository;
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

@ApplicationScoped
public class AnalyticsServiceImpl implements AnalyticsService {

    @Inject
    private BalanceService balanceService;

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceRepository workspaceRepository;

    @Override
    public BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate) {
        // Delegate to BalanceService
        return balanceService.getBalanceSummary(workspaceId, userId, startDate, endDate);
    }

    @Override
    public ExpenseSnapshotResponse getExpensesSnapshot(String workspaceId, LocalDate startDate, LocalDate endDate, int size) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(
                workspaceUuid, startDate, endDate, null, false, 0, Integer.MAX_VALUE, null);
        
        // Group by category - use String key to handle null categoryId
        Map<String, ExpenseSnapshotItem> categoryMap = new HashMap<>();
        
        for (io.evenly.core.domain.Expense expense : expenses) {
            UUID categoryId = expense.getCategoryId();
            String categoryKey = categoryId != null ? categoryId.toString() : "uncategorized";
            
            ExpenseSnapshotItem item = categoryMap.computeIfAbsent(categoryKey, k -> {
                ExpenseSnapshotItem newItem = new ExpenseSnapshotItem();
                newItem.setCategoryId(categoryId != null ? categoryId.toString() : null);
                
                // Fetch category details if categoryId exists
                if (categoryId != null) {
                    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        newItem.setCategoryName(category.getName());
                        newItem.setCategoryIcon(category.getIcon());
                        newItem.setCategoryColor(category.getColor());
                    } else {
                        // Category not found, use defaults
                        newItem.setCategoryName("Unknown");
                        newItem.setCategoryIcon("fa-solid fa-box");
                        newItem.setCategoryColor("#85C1E2");
                    }
                } else {
                    // Uncategorized expenses
                    newItem.setCategoryName("Uncategorized");
                    newItem.setCategoryIcon("fa-solid fa-box");
                    newItem.setCategoryColor("#85C1E2");
                }
                
                newItem.setTotalAmount(BigDecimal.ZERO);
                newItem.setExpensesCount(0);
                return newItem;
            });
            
            item.setTotalAmount(item.getTotalAmount().add(expense.getAmount()));
            item.setExpensesCount(item.getExpensesCount() + 1);
        }
        
        // Calculate total and percentages
        BigDecimal totalAmount = expenses.stream()
            .map(io.evenly.core.domain.Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ExpenseSnapshotItem> items = new ArrayList<>(categoryMap.values());
        for (ExpenseSnapshotItem item : items) {
            BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                ? item.getTotalAmount().divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            item.setSpentPercentage(percentage);
        }
        
        // Sort by amount descending
        items.sort(Comparator.comparing(ExpenseSnapshotItem::getTotalAmount).reversed());
        
        // Apply size limit if specified
        int categoriesCount = items.size();
        int remainingCategoriesCount = 0;
        if (size > 0 && items.size() > size) {
            remainingCategoriesCount = items.size() - size;
            items = items.subList(0, size);
        }
        
        ExpenseSnapshotResponse response = new ExpenseSnapshotResponse();
        response.setData(items);
        response.setCategoriesCount(categoriesCount);
        response.setRemainingCategoriesCount(remainingCategoriesCount);
        
        return response;
    }

    @Override
    public ExpenseSummary getExpensesSummary(String workspaceId, LocalDate startDate, LocalDate endDate) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        // Get workspace for currency
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
                .orElseThrow(() -> new RuntimeException("Workspace not found: " + workspaceId));
        
        String currency = workspace.getCurrency() != null ? workspace.getCurrency().getCode() : "USD";
        
        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(
                workspaceUuid, startDate, endDate, null, false, 0, Integer.MAX_VALUE, null);
        
        // Calculate totals
        BigDecimal totalAmount = expenses.stream()
                .map(io.evenly.core.domain.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int expensesCount = expenses.size();
        
        // Calculate average per day - handle null dates like the mock
        long daysBetween = 1;
        if (startDate != null && endDate != null) {
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } else if (startDate != null) {
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        } else if (endDate != null) {
            // If only endDate is provided, use expenses date range
            if (!expenses.isEmpty()) {
                LocalDate minDate = expenses.stream()
                        .map(io.evenly.core.domain.Expense::getEffectiveDate)
                        .min(LocalDate::compareTo)
                        .orElse(endDate);
                daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, endDate) + 1;
            }
        } else {
            // If no dates provided, use expenses date range
            if (!expenses.isEmpty()) {
                LocalDate minDate = expenses.stream()
                        .map(io.evenly.core.domain.Expense::getEffectiveDate)
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());
                LocalDate maxDate = expenses.stream()
                        .map(io.evenly.core.domain.Expense::getEffectiveDate)
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());
                daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate) + 1;
            }
        }
        
        BigDecimal averagePerDay = daysBetween > 0
                ? totalAmount.divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Find largest expense
        BigDecimal largestExpenseAmount = expenses.stream()
                .map(io.evenly.core.domain.Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Generate linear chart data - aggregate expenses by date
        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        for (io.evenly.core.domain.Expense expense : expenses) {
            dailyTotals.merge(expense.getEffectiveDate(), expense.getAmount(), BigDecimal::add);
        }
        
        // Determine date range for chart
        LocalDate chartStartDate = startDate;
        LocalDate chartEndDate = endDate;
        if (chartStartDate == null || chartEndDate == null) {
            if (!expenses.isEmpty()) {
                if (chartStartDate == null) {
                    chartStartDate = expenses.stream()
                            .map(io.evenly.core.domain.Expense::getEffectiveDate)
                            .min(LocalDate::compareTo)
                            .orElse(LocalDate.now().minusDays(30));
                }
                if (chartEndDate == null) {
                    chartEndDate = expenses.stream()
                            .map(io.evenly.core.domain.Expense::getEffectiveDate)
                            .max(LocalDate::compareTo)
                            .orElse(LocalDate.now());
                }
            } else {
                // No expenses, use default range
                chartStartDate = LocalDate.now().minusDays(30);
                chartEndDate = LocalDate.now();
            }
        }
        
        // Generate data points for all days in range (including days with no expenses)
        // Use daily amounts (not cumulative) for trend visualization
        List<LinearChartDataPoint> linearChartData = new ArrayList<>();
        LocalDate currentDate = chartStartDate;
        
        while (!currentDate.isAfter(chartEndDate)) {
            BigDecimal dayAmount = dailyTotals.getOrDefault(currentDate, BigDecimal.ZERO);
            
            LinearChartDataPoint point = new LinearChartDataPoint();
            point.setDate(currentDate.toString());
            point.setAmount(dayAmount);
            linearChartData.add(point);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Build response
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
