package io.evenly.core.mock.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.categories.dto.Category;
import io.evenly.core.features.expenses.dto.CreateExpenseRequest;
import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.expenses.dto.UpdateExpenseRequest;
import io.evenly.core.mock.data.MockDataProvider;
import io.evenly.core.shared.common.PageInfo;
import io.evenly.core.shared.common.PaginatedExpenses;
import io.evenly.core.shared.common.SortInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import io.evenly.core.features.expenses.ExpenseService;

/**
 \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class ExpenseServiceMock implements ExpenseService {

    @Inject
    private MockDataProvider mockDataProvider;

    @Override
    public PaginatedExpenses findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
            String categoryId, String status, int page, int size, String sort) {
        List<Expense> allExpenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId,
                new ArrayList<>());

        // Apply filters
        List<Expense> filtered = allExpenses.stream()
                .filter(expense -> {
                    if (startDate != null && expense.getEffectiveDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && expense.getEffectiveDate().isAfter(endDate)) {
                        return false;
                    }
                    if (categoryId != null && !categoryId.equals(expense.getCategoryId())) {
                        return false;
                    }
                    if (status != null && !status.equals(expense.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Apply sorting
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String property = sortParts[0].trim();
            String direction = sortParts.length > 1 ? sortParts[1].trim() : "ASC";

            Comparator<Expense> comparator = switch (property.toLowerCase()) {
                case "effectivedate", "date" -> Comparator.comparing(Expense::getEffectiveDate);
                case "amount" -> Comparator.comparing(Expense::getAmount);
                default -> Comparator.comparing(Expense::getEffectiveDate);
            };

            if ("DESC".equalsIgnoreCase(direction)) {
                comparator = comparator.reversed();
            }

            filtered.sort(comparator);
        } else {
            // Default sort by date descending
            filtered.sort(Comparator.comparing(Expense::getEffectiveDate).reversed());
        }

        // Apply pagination
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Expense> pagedData = fromIndex < totalElements
                ? filtered.subList(fromIndex, toIndex)
                : new ArrayList<>();

        PageInfo pageInfo = new PageInfo(page, size, totalElements, totalPages);
        SortInfo sortInfo = new SortInfo(sort != null && !sort.isEmpty(),
                sort != null && sort.contains("DESC") ? "DESC" : "ASC",
                sort != null ? sort.split(",")[0].trim() : "effectiveDate");

        return new PaginatedExpenses(pagedData, pageInfo, sortInfo);
    }

    @Override
    public List<Expense> findRecentForWorkspace(String workspaceId, int size) {
        List<Expense> allExpenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId,
                new ArrayList<>());
        return allExpenses.stream()
                .sorted(Comparator.comparing(Expense::getEffectiveDate).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Expense> findById(String expenseId) {
        return mockDataProvider.getWorkspaceExpenses().values().stream()
                .flatMap(List::stream)
                .filter(expense -> expense.getId().equals(expenseId))
                .findFirst();
    }

    @Override
    public Expense create(String workspaceId, String userId, CreateExpenseRequest request) {
        Expense expense = new Expense();
        expense.setId(workspaceId + "-exp-" + UUID.randomUUID().toString().substring(0, 8));
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setCurrency(mockDataProvider.getWorkspaces().get(workspaceId).getCurrency());
        expense.setEffectiveDate(request.getDate());
        expense.setNote(request.getNote());
        expense.setStatus("ACTIVE");
        expense.setPaidByUserId(userId);

        // Get category info
        List<Category> categories = mockDataProvider.getWorkspaceCategories().getOrDefault(workspaceId,
                new ArrayList<>());
        categories.stream()
                .filter(cat -> cat.getId().equals(request.getCategoryId()))
                .findFirst()
                .ifPresent(cat -> {
                    expense.setCategoryName(cat.getName());
                    expense.setCategoryIcon(cat.getIcon());
                    expense.setCategoryColor(cat.getColor());
                });

        // Get user name
        User user = mockDataProvider.getUsers().get(userId);
        if (user != null) {
            expense.setPaidByUserName(user.getDisplayName());
        }

        mockDataProvider.getWorkspaceExpenses().computeIfAbsent(workspaceId, k -> new ArrayList<>()).add(expense);
        return expense;
    }

    @Override
    public Expense update(String expenseId, String userId, UpdateExpenseRequest request) {
        Optional<Expense> optExpense = findById(expenseId);
        if (optExpense.isEmpty()) {
            throw new RuntimeException("Expense not found");
        }

        Expense expense = optExpense.get();
        if (!"ACTIVE".equals(expense.getStatus())) {
            throw new RuntimeException("Cannot update settled expense");
        }

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getPaidByUserId() != null) {
            expense.setPaidByUserId(request.getPaidByUserId());
            User user = mockDataProvider.getUsers().get(request.getPaidByUserId());
            if (user != null) {
                expense.setPaidByUserName(user.getDisplayName());
            }
        }
        if (request.getCategoryId() != null) {
            expense.setCategoryId(request.getCategoryId());
            // Update category info
            String workspaceId = expenseId.split("-exp-")[0];
            List<Category> categories = mockDataProvider.getWorkspaceCategories().getOrDefault(workspaceId,
                    new ArrayList<>());
            categories.stream()
                    .filter(cat -> cat.getId().equals(request.getCategoryId()))
                    .findFirst()
                    .ifPresent(cat -> {
                        expense.setCategoryName(cat.getName());
                        expense.setCategoryIcon(cat.getIcon());
                        expense.setCategoryColor(cat.getColor());
                    });
        }
        if (request.getDate() != null) {
            expense.setEffectiveDate(request.getDate());
        }
        if (request.getNote() != null) {
            expense.setNote(request.getNote());
        }

        return expense;
    }

    @Override
    public void delete(String expenseId, String userId) {
        Optional<Expense> optExpense = findById(expenseId);
        if (optExpense.isEmpty()) {
            throw new RuntimeException("Expense not found");
        }

        Expense expense = optExpense.get();
        if (!"ACTIVE".equals(expense.getStatus())) {
            throw new RuntimeException("Cannot delete settled expense");
        }

        mockDataProvider.getWorkspaceExpenses().values()
                .forEach(expenses -> expenses.removeIf(e -> e.getId().equals(expenseId)));
    }
}
