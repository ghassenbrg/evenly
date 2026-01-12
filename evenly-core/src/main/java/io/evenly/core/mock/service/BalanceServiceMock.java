package io.evenly.core.mock.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.balance.BalanceService;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.balance.dto.SettleUpMember;
import io.evenly.core.features.balance.dto.SettleUpResponse;
import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.mock.config.MockProfileActivator;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

/**
 * Mock implementation of BalanceService.
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class BalanceServiceMock implements BalanceService {

    @Inject
    private MockDataProvider mockDataProvider;


    @Override
    public List<Balance> getBalanceForWorkspace(String workspaceId) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            return new ArrayList<>();
        }

        List<WorkspaceMember> members = mockDataProvider.getWorkspaceMembers().getOrDefault(workspaceId,
                new ArrayList<>());
        List<Expense> expenses = mockDataProvider.getWorkspaceExpenses().getOrDefault(workspaceId, new ArrayList<>())
                .stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .collect(Collectors.toList());

        Map<String, BigDecimal> paid = new HashMap<>();
        Map<String, BigDecimal> expected = new HashMap<>();

        // Calculate paid amounts
        for (Expense expense : expenses) {
            String paidBy = expense.getPaidByUserId();
            paid.put(paidBy, paid.getOrDefault(paidBy, BigDecimal.ZERO).add(expense.getAmount()));
        }

        // Calculate expected amounts (equal split for now)
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int memberCount = members.size();
        if (memberCount > 0) {
            BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(memberCount), 2,
                    java.math.RoundingMode.HALF_UP);
            for (WorkspaceMember member : members) {
                expected.put(member.getUserId(), perPerson);
            }
        }

        // Build balance list
        List<Balance> balances = new ArrayList<>();
        for (WorkspaceMember member : members) {
            String userId = member.getUserId();
            BigDecimal paidAmount = paid.getOrDefault(userId, BigDecimal.ZERO);
            BigDecimal expectedAmount = expected.getOrDefault(userId, BigDecimal.ZERO);
            BigDecimal balance = paidAmount.subtract(expectedAmount);

            Balance bal = new Balance(userId, paidAmount, expectedAmount, member.getUser());
            bal.setBalance(balance);
            balances.add(bal);
        }

        return balances;
    }

    @Override
    public SettleUpResponse getSettleUpForWorkspace(String workspaceId, String currentUserId) {
        Workspace workspace = mockDataProvider.getWorkspaces().get(workspaceId);
        if (workspace == null) {
            throw new RuntimeException("Workspace not found");
        }

        List<Balance> balances = getBalanceForWorkspace(workspaceId);

        SettleUpMember currentUserMember = null;
        List<SettleUpMember> otherMembers = new ArrayList<>();

        for (Balance balance : balances) {
            SettleUpMember member = new SettleUpMember();
            member.setUserId(balance.getUserId());
            member.setUserFullName(balance.getUser().getDisplayName());
            member.setPaidAmount(balance.getPaid());
            member.setExpectedAmount(balance.getExpected());

            if (balance.getUserId().equals(currentUserId)) {
                currentUserMember = member;
            } else {
                otherMembers.add(member);
            }
        }

        if (currentUserMember == null) {
            throw new RuntimeException("Current user not found in workspace");
        }

        return new SettleUpResponse(workspace.getCurrency(), currentUserMember, otherMembers);
    }

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
}
