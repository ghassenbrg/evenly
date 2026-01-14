package io.evenly.core.mock.service;

import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotResponse;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotItem;
import io.evenly.core.features.analytics.dto.ExpenseSummary;
import io.evenly.core.features.analytics.dto.LinearChartDataPoint;
import io.evenly.core.features.analytics.AnalyticsService;

/**
 \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class AnalyticsServiceMock implements AnalyticsService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        List<Expense> expenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId, new ArrayList<>())
            .stream()
            .filter(e -> {
                if (startDate != null && e.getEffectiveDate().isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && e.getEffectiveDate().isAfter(endDate)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        BigDecimal userExpensePaid = expenses.stream()
            .filter(e -> userId.equals(e.getPaidByUserId()))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<io.evenly.core.features.payments.dto.Payment> payments = mockDataProvider
            .getWorkspacePayments()
            .getOrDefault(workspaceId, new ArrayList<>())
            .stream()
            .filter(p -> {
                if (!"COMPLETED".equals(p.getStatus())) {
                    return false;
                }
                if (startDate != null && p.getEffectiveDate().isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && p.getEffectiveDate().isAfter(endDate)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        BigDecimal paymentsSent = payments.stream()
            .filter(p -> userId.equals(p.getPaidByUserId()))
            .map(io.evenly.core.features.payments.dto.Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paymentsReceived = payments.stream()
            .filter(p -> userId.equals(p.getPayeeUserId()))
            .map(io.evenly.core.features.payments.dto.Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal userTotalPaid = userExpensePaid.add(paymentsSent).subtract(paymentsReceived);
        
        BigDecimal workspaceTotalPaid = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<io.evenly.core.features.workspaces.dto.WorkspaceMember> members = mockDataProvider
            .getWorkspaceMembers()
            .getOrDefault(workspaceId, new ArrayList<>());
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal userWeight = BigDecimal.ZERO;
        for (io.evenly.core.features.workspaces.dto.WorkspaceMember member : members) {
            BigDecimal weight = member.getWeightPercent() != null
                ? BigDecimal.valueOf(member.getWeightPercent())
                : BigDecimal.ZERO;
            totalWeight = totalWeight.add(weight);
            if (userId.equals(member.getUserId())) {
                userWeight = weight;
            }
        }

        BigDecimal userTotalExpected = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            userTotalExpected = workspaceTotalPaid
                .multiply(userWeight)
                .divide(totalWeight, 2, java.math.RoundingMode.HALF_UP);
        } else if (!members.isEmpty()) {
            userTotalExpected = workspaceTotalPaid.divide(BigDecimal.valueOf(members.size()), 2,
                java.math.RoundingMode.HALF_UP);
        }
        
        BigDecimal budgetLimit = workspace.getMonthlySharedLimit() != null 
            ? BigDecimal.valueOf(workspace.getMonthlySharedLimit())
            : null;
        
        BigDecimal spentPercentage = budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) > 0
            ? workspaceTotalPaid.divide(budgetLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        BalanceSummary summary = new BalanceSummary();
        summary.setUserTotalPaidAmount(userTotalPaid);
        summary.setUserTotalExpectedAmount(userTotalExpected);
        summary.setWorkspaceTotalPaidAmount(workspaceTotalPaid);
        summary.setBudgetLimit(budgetLimit);
        summary.setSpentPercentage(spentPercentage);
        summary.setCurrency(workspace.getCurrency());
        
        return summary;
    }
    
    @Override
    public ExpenseSnapshotResponse getExpensesSnapshot(String workspaceId, LocalDate startDate, LocalDate endDate, int size) {
        List<Expense> expenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId, new ArrayList<>())
            .stream()
            .filter(e -> {
                if (startDate != null && e.getEffectiveDate().isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && e.getEffectiveDate().isAfter(endDate)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // Group by category
        Map<String, ExpenseSnapshotItem> categoryMap = new HashMap<>();
        
        for (Expense expense : expenses) {
            String categoryId = expense.getCategoryId() != null ? expense.getCategoryId() : "uncategorized";
            String categoryName = expense.getCategoryName() != null ? expense.getCategoryName() : "Uncategorized";
            
            ExpenseSnapshotItem item = categoryMap.computeIfAbsent(categoryId, k -> {
                ExpenseSnapshotItem newItem = new ExpenseSnapshotItem();
                newItem.setCategoryId(categoryId.equals("uncategorized") ? null : categoryId);
                newItem.setCategoryName(categoryName);
                newItem.setCategoryIcon(expense.getCategoryIcon() != null ? expense.getCategoryIcon() : "fa-solid fa-box");
                newItem.setCategoryColor(expense.getCategoryColor() != null ? expense.getCategoryColor() : "#85C1E2");
                newItem.setTotalAmount(BigDecimal.ZERO);
                newItem.setExpensesCount(0);
                return newItem;
            });
            
            item.setTotalAmount(item.getTotalAmount().add(expense.getAmount()));
            item.setExpensesCount(item.getExpensesCount() + 1);
        }
        
        // Calculate total and percentages
        BigDecimal totalAmount = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ExpenseSnapshotItem> items = new ArrayList<>(categoryMap.values());
        for (ExpenseSnapshotItem item : items) {
            BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                ? item.getTotalAmount().divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
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
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }
        
        // Filter expenses by date range
        List<Expense> expenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId, new ArrayList<>())
            .stream()
            .filter(e -> {
                if (startDate != null && e.getEffectiveDate().isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && e.getEffectiveDate().isAfter(endDate)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal totalAmount = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int expensesCount = expenses.size();
        
        // Calculate average per day
        long daysBetween = 1;
        if (startDate != null && endDate != null) {
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } else if (startDate != null) {
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        } else if (endDate != null) {
            // If only endDate is provided, use expenses date range
            if (!expenses.isEmpty()) {
                LocalDate minDate = expenses.stream()
                    .map(Expense::getEffectiveDate)
                    .min(LocalDate::compareTo)
                    .orElse(endDate);
                daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, endDate) + 1;
            }
        } else {
            // If no dates provided, use expenses date range
            if (!expenses.isEmpty()) {
                LocalDate minDate = expenses.stream()
                    .map(Expense::getEffectiveDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
                LocalDate maxDate = expenses.stream()
                    .map(Expense::getEffectiveDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
                daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate) + 1;
            }
        }
        
        BigDecimal averagePerDay = daysBetween > 0
            ? totalAmount.divide(BigDecimal.valueOf(daysBetween), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // Find largest expense
        BigDecimal largestExpenseAmount = expenses.stream()
            .map(Expense::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        // Generate linear chart data - aggregate expenses by date
        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        for (Expense expense : expenses) {
            dailyTotals.merge(expense.getEffectiveDate(), expense.getAmount(), BigDecimal::add);
        }
        
        // Determine date range for chart
        LocalDate chartStartDate = startDate;
        LocalDate chartEndDate = endDate;
        if (chartStartDate == null || chartEndDate == null) {
            if (!expenses.isEmpty()) {
                if (chartStartDate == null) {
                    chartStartDate = expenses.stream()
                        .map(Expense::getEffectiveDate)
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now().minusDays(30));
                }
                if (chartEndDate == null) {
                    chartEndDate = expenses.stream()
                        .map(Expense::getEffectiveDate)
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
        summary.setCurrency(workspace.getCurrency());
        summary.setLargestExpenseAmount(largestExpenseAmount);
        summary.setLinearChartData(linearChartData);
        
        return summary;
    }
}
