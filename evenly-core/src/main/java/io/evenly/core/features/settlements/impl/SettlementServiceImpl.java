package io.evenly.core.features.settlements.impl;

import io.evenly.core.domain.Expense;
import io.evenly.core.domain.Payment;
import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.domain.repository.SettlementRepository;
import io.evenly.core.features.auth.UserService;
import io.evenly.core.features.balance.BalanceService;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;
import io.evenly.core.shared.exception.ConflictException;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SettlementService implementation.
 */
@ApplicationScoped
public class SettlementServiceImpl implements io.evenly.core.features.settlements.SettlementService {

    @Inject
    private SettlementRepository settlementRepository;

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private UserService userService;

    @Inject
    private BalanceService balanceService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<io.evenly.core.features.settlements.dto.Settlement> findForWorkspace(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        return settlementRepository.findByWorkspaceId(workspaceUuid).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public io.evenly.core.features.settlements.dto.Settlement create(String workspaceId, String userId, CreateSettlementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Settlement request is required");
        }

        UUID workspaceUuid = UUID.fromString(workspaceId);
        boolean hasPeriod = request.getStartDate() != null || request.getEndDate() != null;
        boolean hasExpenseIds = request.getExpenseIds() != null && !request.getExpenseIds().isEmpty();
        boolean hasPaymentIds = request.getPaymentIds() != null && !request.getPaymentIds().isEmpty();
        boolean hasIds = hasExpenseIds || hasPaymentIds;

        if (hasPeriod && hasIds) {
            throw new IllegalArgumentException("Provide either a date range or explicit transaction ids");
        }
        if (!hasPeriod && !hasIds) {
            throw new IllegalArgumentException("Provide a date range or explicit transaction ids");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (hasPeriod) {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Both startDate and endDate are required for period settlements");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("startDate must be on or before endDate");
            }
        }

        List<Expense> expenses = hasPeriod
            ? expenseRepository.findByWorkspaceId(workspaceUuid, startDate, endDate, null, null, 0, Integer.MAX_VALUE, null)
            : resolveExpenses(workspaceUuid, request.getExpenseIds());

        List<Payment> payments = hasPeriod
            ? paymentRepository.findByWorkspaceId(workspaceUuid, startDate, endDate, null, null, 0, Integer.MAX_VALUE, null)
            : resolvePayments(workspaceUuid, request.getPaymentIds());

        if (expenses.isEmpty() && payments.isEmpty()) {
            throw new ConflictException("No expenses or payments found to settle");
        }

        ensureAllUnsettled(expenses, payments);

        io.evenly.core.domain.Settlement settlement = io.evenly.core.domain.Settlement.builder()
            .workspaceId(workspaceUuid)
            .startDate(startDate)
            .endDate(endDate)
            .note(request.getNote())
            .createdByUserId(userId)
            .createdAt(OffsetDateTime.now())
            .build();

        settlement = settlementRepository.save(settlement);
        OffsetDateTime settledAt = OffsetDateTime.now();

        for (Expense expense : expenses) {
            expense.setSettlementId(settlement.getId());
            expense.setSettledAt(settledAt);
            expenseRepository.save(expense);
        }

        for (Payment payment : payments) {
            payment.setSettlementId(settlement.getId());
            payment.setSettledAt(settledAt);
            paymentRepository.save(payment);
        }

        return toDto(settlement);
    }

    @Override
    @Transactional
    public io.evenly.core.features.settlements.dto.Settlement settleAll(String workspaceId, String userId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);

        List<Expense> expenses = findUnsettledExpenses(workspaceUuid);
        List<Payment> payments = findUnsettledPayments(workspaceUuid);

        if (expenses.isEmpty() && payments.isEmpty()) {
            return null;
        }

        io.evenly.core.domain.Settlement settlement = io.evenly.core.domain.Settlement.builder()
            .workspaceId(workspaceUuid)
            .createdByUserId(userId)
            .createdAt(OffsetDateTime.now())
            .build();

        settlement = settlementRepository.save(settlement);
        OffsetDateTime settledAt = OffsetDateTime.now();

        for (Expense expense : expenses) {
            expense.setSettlementId(settlement.getId());
            expense.setSettledAt(settledAt);
            expenseRepository.save(expense);
        }

        for (Payment payment : payments) {
            payment.setSettlementId(settlement.getId());
            payment.setSettledAt(settledAt);
            paymentRepository.save(payment);
        }

        return toDto(settlement);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean canSuggestSettlement(String workspaceId) {
        List<Balance> balances = balanceService.getBalanceForWorkspace(workspaceId);
        return balances.stream()
            .allMatch(balance -> balance.getBalance() != null && balance.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean hasUnsettledRecords(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        boolean hasUnsettledExpenses = expenseRepository.findByWorkspaceId(workspaceUuid).stream()
            .anyMatch(expense -> expense.getSettlementId() == null);
        if (hasUnsettledExpenses) {
            return true;
        }
        return paymentRepository.findByWorkspaceId(workspaceUuid).stream()
            .anyMatch(payment -> payment.getSettlementId() == null);
    }

    @Override
    @Transactional
    public io.evenly.core.features.settlements.dto.Settlement revert(String workspaceId, String settlementId, String userId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        UUID settlementUuid = UUID.fromString(settlementId);

        io.evenly.core.domain.Settlement settlement = settlementRepository.findById(settlementUuid)
            .orElseThrow(() -> new NotFoundException("Settlement not found: " + settlementId));

        if (!settlement.getWorkspaceId().equals(workspaceUuid)) {
            throw new NotFoundException("Settlement not found: " + settlementId);
        }
        if (settlement.getRevertedAt() != null) {
            throw new ConflictException("Settlement is already reverted");
        }

        settlement.setRevertedAt(OffsetDateTime.now());
        settlement.setRevertedByUserId(userId);
        settlementRepository.save(settlement);

        List<Expense> expenses = expenseRepository.findBySettlementId(settlementUuid);
        for (Expense expense : expenses) {
            expense.setSettlementId(null);
            expense.setSettledAt(null);
            expenseRepository.save(expense);
        }

        List<Payment> payments = paymentRepository.findBySettlementId(settlementUuid);
        for (Payment payment : payments) {
            payment.setSettlementId(null);
            payment.setSettledAt(null);
            paymentRepository.save(payment);
        }

        return toDto(settlement);
    }

    private void ensureAllUnsettled(List<Expense> expenses, List<Payment> payments) {
        boolean hasSettledExpense = expenses.stream().anyMatch(expense -> expense.getSettlementId() != null);
        boolean hasSettledPayment = payments.stream().anyMatch(payment -> payment.getSettlementId() != null);
        if (hasSettledExpense || hasSettledPayment) {
            throw new ConflictException("Some records are already settled");
        }
    }

    private List<Expense> findUnsettledExpenses(UUID workspaceUuid) {
        return expenseRepository.findByWorkspaceId(workspaceUuid).stream()
            .filter(expense -> expense.getSettlementId() == null)
            .collect(Collectors.toList());
    }

    private List<Payment> findUnsettledPayments(UUID workspaceUuid) {
        return paymentRepository.findByWorkspaceId(workspaceUuid).stream()
            .filter(payment -> payment.getSettlementId() == null)
            .collect(Collectors.toList());
    }

    private List<Expense> resolveExpenses(UUID workspaceUuid, List<String> expenseIds) {
        if (expenseIds == null || expenseIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Expense> expenses = new ArrayList<>();
        for (String expenseId : expenseIds) {
            UUID expenseUuid = UUID.fromString(expenseId);
            Expense expense = expenseRepository.findById(expenseUuid)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + expenseId));
            if (!expense.getWorkspaceId().equals(workspaceUuid)) {
                throw new NotFoundException("Expense not found: " + expenseId);
            }
            expenses.add(expense);
        }
        return expenses;
    }

    private List<Payment> resolvePayments(UUID workspaceUuid, List<String> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Payment> payments = new ArrayList<>();
        for (String paymentId : paymentIds) {
            UUID paymentUuid = UUID.fromString(paymentId);
            Payment payment = paymentRepository.findById(paymentUuid)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
            if (!payment.getWorkspaceId().equals(workspaceUuid)) {
                throw new NotFoundException("Payment not found: " + paymentId);
            }
            payments.add(payment);
        }
        return payments;
    }

    private io.evenly.core.features.settlements.dto.Settlement toDto(io.evenly.core.domain.Settlement settlement) {
        io.evenly.core.features.settlements.dto.Settlement dto = new io.evenly.core.features.settlements.dto.Settlement();
        dto.setId(settlement.getId().toString());
        dto.setWorkspaceId(settlement.getWorkspaceId().toString());
        dto.setCreatedByUserId(settlement.getCreatedByUserId());
        dto.setCreatedAt(settlement.getCreatedAt());
        dto.setStartDate(settlement.getStartDate());
        dto.setEndDate(settlement.getEndDate());
        dto.setNote(settlement.getNote());
        dto.setRevertedAt(settlement.getRevertedAt());
        dto.setRevertedByUserId(settlement.getRevertedByUserId());

        userService.findById(settlement.getCreatedByUserId()).ifPresent(dto::setCreatedBy);
        return dto;
    }
}
