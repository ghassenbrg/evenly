package io.evenly.core.features.analytics;

import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.shared.common.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotResponse;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotItem;

/**
 * Mock implementation of AnalyticsService.
 */
@ApplicationScoped
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
        
        BigDecimal userTotalPaid = expenses.stream()
            .filter(e -> userId.equals(e.getPaidByUserId()))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal workspaceTotalPaid = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int memberCount = mockDataProvider.getWorkspaceMembers().getOrDefault(workspaceId, new ArrayList<>()).size();
        BigDecimal userTotalExpected = memberCount > 0 
            ? workspaceTotalPaid.divide(BigDecimal.valueOf(memberCount), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
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
}
