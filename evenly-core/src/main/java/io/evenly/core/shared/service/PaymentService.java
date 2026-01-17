package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.PaginatedPayments;
import io.evenly.core.shared.dto.Payment;
import io.evenly.core.shared.dto.request.CreatePaymentRequest;
import io.evenly.core.shared.dto.request.UpdatePaymentRequest;
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
    Payment update(String paymentId, UpdatePaymentRequest request);
    void delete(String paymentId);
}
