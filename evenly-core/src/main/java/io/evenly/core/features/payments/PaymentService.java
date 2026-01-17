package io.evenly.core.features.payments;

import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.payments.dto.CreatePaymentRequest;
import io.evenly.core.features.payments.dto.UpdatePaymentRequest;
import io.evenly.core.shared.common.PaginatedPayments;
import io.evenly.core.shared.common.SettlementScope;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for payment operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface PaymentService {
    PaginatedPayments findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
                                       String status, SettlementScope settlementScope, int page, int size, String sort);
    Optional<Payment> findById(String paymentId);
    Payment create(String workspaceId, String userId, CreatePaymentRequest request);
    Payment update(String paymentId, String userId, UpdatePaymentRequest request);
    void delete(String paymentId, String userId);
}
