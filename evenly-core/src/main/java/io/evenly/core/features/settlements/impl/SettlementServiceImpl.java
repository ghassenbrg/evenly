package io.evenly.core.features.settlements.impl;

import io.evenly.core.domain.Payment;
import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.features.balance.BalanceService;
import io.evenly.core.features.balance.dto.Balance;
import io.evenly.core.features.currencies.SupportedCurrency;
import io.evenly.core.features.settlements.dto.CreateSettlementRequest;
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SettlementService implementation.
 * Note: Based on the schema, settlements and transfers were removed.
 * This service works with payments instead.
 */
@ApplicationScoped
public class SettlementServiceImpl implements io.evenly.core.features.settlements.SettlementService {

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceRepository workspaceRepository;

    @Inject
    private BalanceService balanceService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Settlement> findForWorkspace(String workspaceId) {
        // Since settlements table was removed, we return empty list
        // or could return payments as settlements
        return List.of();
    }

    @Override
    @Transactional
    public Settlement create(String workspaceId, String userId, CreateSettlementRequest request) { // userId is now username (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get workspace to get currency
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));

        SupportedCurrency currency = workspace.getCurrency() != null ? workspace.getCurrency() : SupportedCurrency.USD;

        // Get balances for all users in the workspace
        List<Balance> balances = balanceService.getBalanceForWorkspace(workspaceId);

        // Filter out users with zero balance and separate into creditors (positive balance) and debtors (negative balance)
        List<Balance> creditors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(Balance::getBalance).reversed())
                .collect(Collectors.toList());

        List<Balance> debtors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(Balance::getBalance))
                .collect(Collectors.toList());

        // Create payments using a simplified debt settlement algorithm
        // Match creditors with debtors to minimize the number of payments
        List<Payment> createdPayments = new ArrayList<>();
        int creditorIndex = 0;
        int debtorIndex = 0;

        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            Balance creditor = creditors.get(creditorIndex);
            Balance debtor = debtors.get(debtorIndex);

            BigDecimal creditorBalance = creditor.getBalance();
            BigDecimal debtorBalance = debtor.getBalance().abs(); // Make positive for comparison

            BigDecimal paymentAmount = creditorBalance.min(debtorBalance);

            // Create payment: debtor pays creditor
            Payment payment = Payment.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(workspaceUuid)
                    .paidByUserId(debtor.getUserId()) // Person who owes money pays
                    .payeeUserId(creditor.getUserId()) // Person who is owed money receives
                    .amount(paymentAmount)
                    .currency(currency)
                    .effectiveDate(LocalDate.now())
                    .note(request.getNote())
                    .status("COMPLETED")
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            payment = paymentRepository.save(payment);
            createdPayments.add(payment);

            // Update balances
            creditorBalance = creditorBalance.subtract(paymentAmount);
            debtorBalance = debtorBalance.subtract(paymentAmount);

            // Update creditor balance
            creditor.setBalance(creditorBalance);
            if (creditorBalance.compareTo(BigDecimal.ZERO) <= 0) {
                creditorIndex++;
            }

            // Update debtor balance
            debtor.setBalance(debtorBalance.negate());
            if (debtorBalance.compareTo(BigDecimal.ZERO) <= 0) {
                debtorIndex++;
            }
        }

        // Create settlement DTO
        Settlement result = new Settlement();
        result.setId(UUID.randomUUID().toString());
        result.setWorkspaceId(workspaceId);
        result.setCreatedByUserId(userId);
        result.setCreatedAt(OffsetDateTime.now());
        return result;
    }
}
