package io.evenly.core.mock.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.payments.dto.CreatePaymentRequest;
import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.payments.dto.UpdatePaymentRequest;
import io.evenly.core.mock.data.MockDataProvider;
import io.evenly.core.shared.common.PageInfo;
import io.evenly.core.shared.common.PaginatedPayments;
import io.evenly.core.shared.common.SortInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import io.evenly.core.features.payments.PaymentService;

/**
 \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class PaymentServiceMock implements PaymentService {

    @Inject
    private MockDataProvider mockDataProvider;

    @Override
    public PaginatedPayments findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
            String status, int page, int size, String sort) {
        List<Payment> allPayments = mockDataProvider.getWorkspacePayments().getOrDefault(workspaceId,
                new ArrayList<>());

        // Apply filters
        List<Payment> filtered = allPayments.stream()
                .filter(payment -> {
                    if (startDate != null && payment.getEffectiveDate() != null
                            && payment.getEffectiveDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && payment.getEffectiveDate() != null
                            && payment.getEffectiveDate().isAfter(endDate)) {
                        return false;
                    }
                    if (status != null && !status.equals(payment.getStatus())) {
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

            Comparator<Payment> comparator = switch (property.toLowerCase()) {
                case "effectivedate", "date" ->
                    Comparator.comparing(Payment::getEffectiveDate, Comparator.nullsLast(Comparator.naturalOrder()));
                case "amount" -> Comparator.comparing(Payment::getAmount);
                default ->
                    Comparator.comparing(Payment::getEffectiveDate, Comparator.nullsLast(Comparator.naturalOrder()));
            };

            if ("DESC".equalsIgnoreCase(direction)) {
                comparator = comparator.reversed();
            }

            filtered.sort(comparator);
        } else {
            filtered.sort(
                    Comparator.comparing(Payment::getEffectiveDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Apply pagination
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Payment> pagedData = fromIndex < totalElements
                ? filtered.subList(fromIndex, toIndex)
                : new ArrayList<>();

        PageInfo pageInfo = new PageInfo(page, size, totalElements, totalPages);
        SortInfo sortInfo = new SortInfo(sort != null && !sort.isEmpty(),
                sort != null && sort.contains("DESC") ? "DESC" : "ASC",
                sort != null ? sort.split(",")[0].trim() : "effectiveDate");

        return new PaginatedPayments(pagedData, pageInfo, sortInfo);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return mockDataProvider.getWorkspacePayments().values().stream()
                .flatMap(List::stream)
                .filter(payment -> payment.getId().equals(paymentId))
                .findFirst();
    }

    @Override
    public Payment create(String workspaceId, String userId, CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setId(workspaceId + "-pay-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setPayeeUserId(request.getPayeeUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(mockDataProvider.getWorkspaces().get(workspaceId).getCurrency());
        payment.setEffectiveDate(request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now());
        payment.setNote(request.getNote());
        payment.setStatus("PENDING");
        payment.setPaidByUserId(userId);

        // Get user names
        User payee = mockDataProvider.getUsers().get(request.getPayeeUserId());
        if (payee != null) {
            payment.setPayeeUserName(payee.getDisplayName());
        }
        User payer = mockDataProvider.getUsers().get(userId);
        if (payer != null) {
            payment.setPaidByUserName(payer.getDisplayName());
        }

        mockDataProvider.getWorkspacePayments().computeIfAbsent(workspaceId, k -> new ArrayList<>()).add(payment);
        return payment;
    }

    @Override
    public Payment update(String paymentId, UpdatePaymentRequest request) {
        Optional<Payment> optPayment = findById(paymentId);
        if (optPayment.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        Payment payment = optPayment.get();

        if (request.getPayeeUserId() != null) {
            payment.setPayeeUserId(request.getPayeeUserId());
            User payee = mockDataProvider.getUsers().get(request.getPayeeUserId());
            if (payee != null) {
                payment.setPayeeUserName(payee.getDisplayName());
            }
        }
        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
        }
        if (request.getEffectiveDate() != null) {
            payment.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getNote() != null) {
            payment.setNote(request.getNote());
        }
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
        }

        return payment;
    }

    @Override
    public void delete(String paymentId) {
        Optional<Payment> optPayment = findById(paymentId);
        if (optPayment.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        mockDataProvider.getWorkspacePayments().values()
                .forEach(payments -> payments.removeIf(p -> p.getId().equals(paymentId)));
    }
}
