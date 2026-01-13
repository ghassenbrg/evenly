package io.evenly.core.features.balance.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.evenly.core.domain.ExpenseParticipant;
import io.evenly.core.domain.Payment;
import io.evenly.core.domain.repository.ExpenseParticipantRepository;
import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.features.analytics.dto.BalanceSummary;
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

    @Override
    public List<Balance> getBalanceForWorkspace(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get all expenses for workspace
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid);

        // Get all payments for workspace
        List<Payment> payments = paymentRepository.findByWorkspaceId(workspaceUuid);

        // Calculate balances per user
        Map<String, BigDecimal> balances = new HashMap<>(); // userId is now String (username)

        // Process expenses: paid_by gets +amount, participants get
        // -amount/participantCount
        for (io.evenly.core.domain.Expense expense : expenses) {
            List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
            int participantCount = Math.max(participants.size(), 1);
            BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                    BigDecimal.ROUND_HALF_UP);

            // Person who paid gets the full amount
            balances.merge(expense.getPaidByUserId(), expense.getAmount(), BigDecimal::add); // userId is now String

            // Participants owe their share
            for (ExpenseParticipant participant : participants) {
                balances.merge(participant.getUserId(), perParticipant.negate(), BigDecimal::add); // userId is now
                                                                                                   // String
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

        // Convert to Balance DTOs
        return balances.entrySet().stream()
                .map(entry -> {
                    Balance balance = new Balance();
                    balance.setUserId(entry.getKey()); // userId is now String, no need to convert
                    balance.setBalance(entry.getValue());
                    // TODO: Calculate paid and expected separately if needed
                    balance.setPaid(BigDecimal.ZERO);
                    balance.setExpected(BigDecimal.ZERO);
                    return balance;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SettleUpResponse getSettleUpForWorkspace(String workspaceId, String currentUserId) { // userId is now
                                                                                                // username (String)
        List<Balance> balances = getBalanceForWorkspace(workspaceId);

        // Find current user's balance
        Balance currentUserBalance = balances.stream()
                .filter(b -> b.getUserId().equals(currentUserId)) // userId is now String
                .findFirst()
                .orElse(new Balance());

        SettleUpResponse response = new SettleUpResponse();
        // TODO: Set currency from workspace
        // TODO: Create SettleUpMember for current user
        // TODO: Create SettleUpMember list for other members

        // Calculate who owes whom
        List<Balance> debts = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0 && !b.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        List<Balance> credits = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0 && !b.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        // TODO: Convert to SettleUpMember format

        return response;
    }

    @Override
    public BalanceSummary getBalanceSummary(String workspaceId, String userId, LocalDate startDate, LocalDate endDate) { // userId
                                                                                                                         // is
                                                                                                                         // now
                                                                                                                         // username
                                                                                                                         // (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get expenses in date range
        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(workspaceUuid, startDate,
                endDate, null, 0, Integer.MAX_VALUE, null);

        BigDecimal totalPaid = expenses.stream()
                .filter(e -> e.getPaidByUserId().equals(userId)) // userId is now String
                .map(io.evenly.core.domain.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOwed = BigDecimal.ZERO;
        for (io.evenly.core.domain.Expense expense : expenses) {
            List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
            int participantCount = Math.max(participants.size(), 1);
            BigDecimal perParticipant = expense.getAmount().divide(BigDecimal.valueOf(participantCount), 2,
                    BigDecimal.ROUND_HALF_UP);

            if (participants.stream().anyMatch(p -> p.getUserId().equals(userId))) { // userId is now String
                totalOwed = totalOwed.add(perParticipant);
            }
        }

        BalanceSummary summary = new BalanceSummary();
        summary.setUserTotalPaidAmount(totalPaid);
        summary.setUserTotalExpectedAmount(totalOwed);
        // TODO: Calculate workspace total and budget info
        return summary;
    }
}
