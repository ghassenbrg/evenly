package io.evenly.core.features.balance.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.evenly.core.domain.ExpenseParticipant;
import io.evenly.core.domain.Payment;
import io.evenly.core.domain.Workspace;
import io.evenly.core.domain.WorkspaceMember;
import io.evenly.core.domain.repository.ExpenseParticipantRepository;
import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.domain.repository.WorkspaceRepository;
import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.auth.UserService;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.balance.dto.SettleUpResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BalanceServiceImpl implements io.evenly.core.features.balance.BalanceService {

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private ExpenseParticipantRepository expenseParticipantRepository;

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Inject
    private WorkspaceRepository workspaceRepository;

    @Inject
    private UserService userService;

    @Override
    public List<Balance> getBalanceForWorkspace(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get workspace to check split mode
        Workspace workspace = workspaceRepository.findById(workspaceUuid)
                .orElseThrow(() -> new RuntimeException("Workspace not found: " + workspaceId));
        
        boolean isWeighted = "WEIGHTED".equals(workspace.getDefaultSplitMode());
        
        // Get workspace members with weights (needed for weighted splits)
        Map<String, BigDecimal> memberWeights = new HashMap<>();
        if (isWeighted) {
            List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceUuid);
            for (WorkspaceMember member : members) {
                memberWeights.put(member.getUserId(), member.getWeightPercent());
            }
        }

        // Get all expenses for workspace
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid);

        // Get all payments for workspace
        List<Payment> payments = paymentRepository.findByWorkspaceId(workspaceUuid);

        // Calculate balances per user
        Map<String, BigDecimal> balances = new HashMap<>(); // userId is now String (username)

        // Process expenses: paid_by gets +amount, participants get their share based on split mode
        for (io.evenly.core.domain.Expense expense : expenses) {
            List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
            
            // Person who paid gets the full amount
            balances.merge(expense.getPaidByUserId(), expense.getAmount(), BigDecimal::add); // userId is now String

            // Calculate each participant's share
            if (isWeighted && !participants.isEmpty()) {
                // Weighted split: calculate based on member weights
                BigDecimal totalWeight = BigDecimal.ZERO;
                for (ExpenseParticipant participant : participants) {
                    BigDecimal weight = memberWeights.getOrDefault(participant.getUserId(), BigDecimal.ZERO);
                    totalWeight = totalWeight.add(weight);
                }
                
                // If total weight is zero, fall back to equal split
                if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                    for (ExpenseParticipant participant : participants) {
                        BigDecimal weight = memberWeights.getOrDefault(participant.getUserId(), BigDecimal.ZERO);
                        BigDecimal share = expense.getAmount()
                                .multiply(weight)
                                .divide(totalWeight, 2, RoundingMode.HALF_UP);
                        balances.merge(participant.getUserId(), share.negate(), BigDecimal::add);
                    }
                } else {
                    // Fallback to equal split if weights are zero
                    int participantCount = participants.size();
                    BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                            RoundingMode.HALF_UP);
                    for (ExpenseParticipant participant : participants) {
                        balances.merge(participant.getUserId(), perParticipant.negate(), BigDecimal::add);
                    }
                }
            } else {
                // Equal split
                int participantCount = Math.max(participants.size(), 1);
                BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                        RoundingMode.HALF_UP);
                for (ExpenseParticipant participant : participants) {
                    balances.merge(participant.getUserId(), perParticipant.negate(), BigDecimal::add);
                }
            }
        }

        // Process payments: paidBy gets -amount, payee gets +amount
        for (Payment payment : payments) {
            if ("COMPLETED".equals(payment.getStatus())) {
                balances.merge(payment.getPaidByUserId(), payment.getAmount().negate(), BigDecimal::add); // userId is
                                                                                                          // now String
                balances.merge(payment.getPayeeUserId(), payment.getAmount(), BigDecimal::add); // userId is now String
            }
        }

        // Calculate paid and expected amounts separately
        Map<String, BigDecimal> paidAmounts = new HashMap<>();
        Map<String, BigDecimal> expectedAmounts = new HashMap<>();

        for (io.evenly.core.domain.Expense expense : expenses) {
            // Person who paid
            paidAmounts.merge(expense.getPaidByUserId(), expense.getAmount(), BigDecimal::add);

            // Participants owe their share based on split mode
            List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
            
            if (isWeighted && !participants.isEmpty()) {
                // Weighted split
                BigDecimal totalWeight = BigDecimal.ZERO;
                for (ExpenseParticipant participant : participants) {
                    BigDecimal weight = memberWeights.getOrDefault(participant.getUserId(), BigDecimal.ZERO);
                    totalWeight = totalWeight.add(weight);
                }
                
                if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                    for (ExpenseParticipant participant : participants) {
                        BigDecimal weight = memberWeights.getOrDefault(participant.getUserId(), BigDecimal.ZERO);
                        BigDecimal share = expense.getAmount()
                                .multiply(weight)
                                .divide(totalWeight, 2, RoundingMode.HALF_UP);
                        expectedAmounts.merge(participant.getUserId(), share, BigDecimal::add);
                    }
                } else {
                    // Fallback to equal split
                    int participantCount = participants.size();
                    BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                            RoundingMode.HALF_UP);
                    for (ExpenseParticipant participant : participants) {
                        expectedAmounts.merge(participant.getUserId(), perParticipant, BigDecimal::add);
                    }
                }
            } else {
                // Equal split
                int participantCount = Math.max(participants.size(), 1);
                BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                        RoundingMode.HALF_UP);
                for (ExpenseParticipant participant : participants) {
                    expectedAmounts.merge(participant.getUserId(), perParticipant, BigDecimal::add);
                }
            }
        }

        // Convert to Balance DTOs with user information
        return balances.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    Balance balance = new Balance();
                    balance.setUserId(userId);
                    balance.setBalance(entry.getValue());
                    balance.setPaid(paidAmounts.getOrDefault(userId, BigDecimal.ZERO));
                    balance.setExpected(expectedAmounts.getOrDefault(userId, BigDecimal.ZERO));

                    // Get user information
                    userService.findById(userId).ifPresent(balance::setUser);

                    return balance;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SettleUpResponse getSettleUpForWorkspace(String workspaceId, String currentUserId) { // userId is now
                                                                                                // username (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);
        Workspace workspace = workspaceRepository.findById(workspaceUuid)
                .orElseThrow(() -> new RuntimeException("Workspace not found: " + workspaceId));

        String currency = workspace.getCurrency() != null ? workspace.getCurrency().getCode() : "USD";

        List<Balance> balances = getBalanceForWorkspace(workspaceId);

        // Find current user's balance
        Balance currentUserBalance = balances.stream()
                .filter(b -> b.getUserId().equals(currentUserId)) // userId is now String
                .findFirst()
                .orElse(new Balance());

        // Create SettleUpMember for current user
        io.evenly.core.features.balance.dto.SettleUpMember currentUserMember = new io.evenly.core.features.balance.dto.SettleUpMember();
        currentUserMember.setUserId(currentUserId);
        userService.findById(currentUserId).ifPresent(user -> {
            currentUserMember.setUserFullName(user.getDisplayName());
        });
        currentUserMember.setPaidAmount(currentUserBalance.getPaid());
        currentUserMember.setExpectedAmount(currentUserBalance.getExpected());

        // Create SettleUpMember list for other members
        List<io.evenly.core.features.balance.dto.SettleUpMember> otherMembers = balances.stream()
                .filter(b -> !b.getUserId().equals(currentUserId))
                .map(balance -> {
                    io.evenly.core.features.balance.dto.SettleUpMember member = new io.evenly.core.features.balance.dto.SettleUpMember();
                    member.setUserId(balance.getUserId());
                    if (balance.getUser() != null) {
                        member.setUserFullName(balance.getUser().getDisplayName());
                    }
                    member.setPaidAmount(balance.getPaid());
                    member.setExpectedAmount(balance.getExpected());
                    return member;
                })
                .collect(Collectors.toList());

        SettleUpResponse response = new SettleUpResponse();
        response.setCurrency(currency);
        response.setCurrentUser(currentUserMember);
        response.setOtherMembers(otherMembers);

        return response;
    }

    @Override
    public BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate) {
        
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get workspace for currency and budget limit
        Workspace workspace = workspaceRepository.findById(workspaceUuid)
                .orElseThrow(() -> new RuntimeException("Workspace not found: " + workspaceId));

        String currency = workspace.getCurrency() != null ? workspace.getCurrency().getCode() : "USD";
        BigDecimal budgetLimit = workspace.getMonthlySharedLimit();

        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid, startDate,
                endDate, null, 0, Integer.MAX_VALUE, null);

        BigDecimal userTotalPaid = expenses.stream()
                .filter(e -> e.getPaidByUserId().equals(userId)) // userId is now String
                .map(io.evenly.core.domain.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get workspace members with weights (needed for weighted splits)
        boolean isWeighted = "WEIGHTED".equals(workspace.getDefaultSplitMode());
        Map<String, BigDecimal> memberWeights = new HashMap<>();
        if (isWeighted) {
            List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceUuid);
            for (WorkspaceMember member : members) {
                memberWeights.put(member.getUserId(), member.getWeightPercent());
            }
        }

        BigDecimal userTotalOwed = BigDecimal.ZERO;
        BigDecimal workspaceTotalPaid = BigDecimal.ZERO;

        for (io.evenly.core.domain.Expense expense : expenses) {
            workspaceTotalPaid = workspaceTotalPaid.add(expense.getAmount());

            List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
            
            // Check if user is a participant
            boolean isParticipant = participants.stream().anyMatch(p -> p.getUserId().equals(userId));
            
            if (isParticipant) {
                BigDecimal userShare;
                if (isWeighted && !participants.isEmpty()) {
                    // Weighted split: calculate based on member weights
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (ExpenseParticipant participant : participants) {
                        BigDecimal weight = memberWeights.getOrDefault(participant.getUserId(), BigDecimal.ZERO);
                        totalWeight = totalWeight.add(weight);
                    }
                    
                    if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal userWeight = memberWeights.getOrDefault(userId, BigDecimal.ZERO);
                        userShare = expense.getAmount()
                                .multiply(userWeight)
                                .divide(totalWeight, 2, RoundingMode.HALF_UP);
                    } else {
                        // Fallback to equal split
                        int participantCount = participants.size();
                        userShare = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                                RoundingMode.HALF_UP);
                    }
                } else {
                    // Equal split
                    int participantCount = Math.max(participants.size(), 1);
                    userShare = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                            RoundingMode.HALF_UP);
                }
                userTotalOwed = userTotalOwed.add(userShare);
            }
        }

        // Calculate spent percentage
        BigDecimal spentPercentage = BigDecimal.ZERO;
        if (budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            spentPercentage = workspaceTotalPaid.divide(budgetLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        BalanceSummary summary = new BalanceSummary();
        summary.setUserTotalPaidAmount(userTotalPaid);
        summary.setUserTotalExpectedAmount(userTotalOwed);
        summary.setWorkspaceTotalPaidAmount(workspaceTotalPaid);
        summary.setBudgetLimit(budgetLimit);
        summary.setSpentPercentage(spentPercentage);
        summary.setCurrency(currency);

        return summary;
    }
}
