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
import java.util.stream.Collectors;

@ApplicationScoped
public class AnalyticsServiceImpl implements AnalyticsService {

    @Inject
    private BalanceService balanceService;

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private CategoryRepository categoryRepository;

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
        String currency = expenses.get(0).getCurrency() != null ? expenses.get(0).getCurrency().getCode() : null;
        
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
