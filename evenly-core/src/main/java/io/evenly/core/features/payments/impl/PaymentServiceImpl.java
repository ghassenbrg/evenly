package io.evenly.core.features.payments.impl;

import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.domain.repository.WorkspaceRepository;
import io.evenly.core.domain.NotificationType;
import io.evenly.core.features.notifications.NotificationService;
import io.evenly.core.features.payments.PaymentService;
import io.evenly.core.features.payments.dto.CreatePaymentRequest;
import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.payments.dto.UpdatePaymentRequest;
import io.evenly.core.shared.common.PageInfo;
import io.evenly.core.shared.common.PaginatedPayments;
import io.evenly.core.shared.common.SettlementScope;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PaymentServiceImpl implements PaymentService {

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private WorkspaceRepository workspaceRepository;

    @Inject
    private NotificationService notificationService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedPayments findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
                                             String status, SettlementScope settlementScope, int page, int size, String sort) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        SettlementScope resolvedScope = resolveSettlementScope(settlementScope, SettlementScope.ALL);

        List<io.evenly.core.domain.Payment> domainPayments = paymentRepository.findByWorkspaceId(
            workspaceUuid, startDate, endDate, status, resolvedScope, page, size, sort);

        long total = paymentRepository.countByWorkspaceId(workspaceUuid, resolvedScope);

        List<io.evenly.core.features.payments.dto.Payment> paymentDtos = domainPayments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        PaginatedPayments result = new PaginatedPayments();
        result.setData(paymentDtos);
        result.setPage(new PageInfo(page, size, (int) total, (int) Math.ceil((double) total / size)));
        io.evenly.core.shared.common.SortInfo sortInfo = new io.evenly.core.shared.common.SortInfo();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            sortInfo.setProperty(parts[0].trim());
            sortInfo.setDirection(parts.length > 1 ? parts[1].trim() : "ASC");
            sortInfo.setSorted(true);
        } else {
            sortInfo.setProperty("effectiveDate");
            sortInfo.setDirection("DESC");
            sortInfo.setSorted(true);
        }
        result.setSort(sortInfo);
        return result;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<io.evenly.core.features.payments.dto.Payment> findById(String paymentId) {
        UUID paymentUuid = UUID.fromString(paymentId);
        return paymentRepository.findById(paymentUuid)
            .map(this::toDto);
    }

    @Override
    @Transactional
    public io.evenly.core.features.payments.dto.Payment create(String workspaceId, String userId, CreatePaymentRequest request) { // userId is now username (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);
        String payeeUserId = request.getPayeeUserId(); // userId is now username (String)

        // Get workspace to get currency
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));

        io.evenly.core.domain.Payment payment = io.evenly.core.domain.Payment.builder()
            .workspaceId(workspaceUuid)
            .paidByUserId(userId) // userId is now username (String)
            .payeeUserId(payeeUserId) // userId is now username (String)
            .amount(request.getAmount())
            .currency(workspace.getCurrency())
            .effectiveDate(request.getEffectiveDate() != null ? request.getEffectiveDate() : java.time.LocalDate.now())
            .note(request.getNote())
            .status("COMPLETED")
            .build();

        payment = paymentRepository.save(payment);

        notificationService.notifyPaymentEvent(workspaceId, userId, payment.getId().toString(), payeeUserId,
            NotificationType.PAYMENT_CREATED, buildPaymentDetail(payment, userId));
        return toDto(payment);
    }

    @Override
    @Transactional
    public io.evenly.core.features.payments.dto.Payment update(String paymentId, String userId, UpdatePaymentRequest request) {
        UUID paymentUuid = UUID.fromString(paymentId);
        io.evenly.core.domain.Payment payment = paymentRepository.findById(paymentUuid)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        ensureNotSettled(payment);

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

        payment = paymentRepository.save(payment);

        String otherParty = resolveOtherParty(payment, userId);
        notificationService.notifyPaymentEvent(payment.getWorkspaceId().toString(), userId, paymentId, otherParty,
            NotificationType.PAYMENT_UPDATED, buildPaymentDetail(payment, userId));
        return toDto(payment);
    }

    @Override
    @Transactional
    public void delete(String paymentId, String userId) {
        UUID paymentUuid = UUID.fromString(paymentId);
        io.evenly.core.domain.Payment payment = paymentRepository.findById(paymentUuid)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        ensureNotSettled(payment);
        paymentRepository.delete(paymentUuid);

        String otherParty = resolveOtherParty(payment, userId);
        notificationService.notifyPaymentEvent(payment.getWorkspaceId().toString(), userId, paymentId, otherParty,
            NotificationType.PAYMENT_DELETED, buildPaymentDetail(payment, userId));
    }

    private String resolveOtherParty(io.evenly.core.domain.Payment payment, String actorUserId) {
        if (actorUserId == null || payment == null) {
            return null;
        }
        if (actorUserId.equals(payment.getPaidByUserId())) {
            return payment.getPayeeUserId();
        }
        if (actorUserId.equals(payment.getPayeeUserId())) {
            return payment.getPaidByUserId();
        }
        return null;
    }

    private String buildPaymentDetail(io.evenly.core.domain.Payment payment, String actorUserId) {
        if (payment == null) {
            return "Payment updated";
        }
        String amount = payment.getAmount() != null ? payment.getAmount().stripTrailingZeros().toPlainString() : null;
        String currency = payment.getCurrency() != null ? payment.getCurrency().getCode() : null;
        String otherPartyId = resolveOtherParty(payment, actorUserId);
        String otherPartyName = otherPartyId != null
            ? userRepository.findById(otherPartyId).map(io.evenly.core.domain.User::getDisplayName).orElse(otherPartyId)
            : "member";

        StringBuilder detail = new StringBuilder();
        if (amount != null) {
            detail.append("Payment of ").append(amount);
            if (currency != null) {
                detail.append(" ").append(currency);
            }
        } else {
            detail.append("Payment updated");
        }
        if (otherPartyName != null) {
            if (actorUserId != null && actorUserId.equals(payment.getPaidByUserId())) {
                detail.append(" to ").append(otherPartyName);
            } else if (actorUserId != null && actorUserId.equals(payment.getPayeeUserId())) {
                detail.append(" from ").append(otherPartyName);
            } else {
                detail.append(" with ").append(otherPartyName);
            }
        }
        return detail.toString();
    }

    private io.evenly.core.features.payments.dto.Payment toDto(io.evenly.core.domain.Payment domain) {
        io.evenly.core.features.payments.dto.Payment dto = new io.evenly.core.features.payments.dto.Payment();
        dto.setId(domain.getId().toString());
        dto.setAmount(domain.getAmount());
        dto.setCurrency(domain.getCurrency() != null ? domain.getCurrency().getCode() : null);
        dto.setEffectiveDate(domain.getEffectiveDate());
        dto.setNote(domain.getNote());
        dto.setStatus(domain.getStatus());
        dto.setPaidByUserId(domain.getPaidByUserId()); // userId is now String, no need to convert
        dto.setPayeeUserId(domain.getPayeeUserId()); // userId is now String, no need to convert
        dto.setSettlementId(domain.getSettlementId() != null ? domain.getSettlementId().toString() : null);
        dto.setSettledAt(domain.getSettledAt());

        userRepository.findById(domain.getPaidByUserId()).ifPresent((io.evenly.core.domain.User user) -> { // userId is now String
            dto.setPaidByUserName(user.getDisplayName());
        });

        userRepository.findById(domain.getPayeeUserId()).ifPresent((io.evenly.core.domain.User user) -> { // userId is now String
            dto.setPayeeUserName(user.getDisplayName());
        });

        return dto;
    }

    private void ensureNotSettled(io.evenly.core.domain.Payment payment) {
        if (payment.getSettlementId() != null) {
            throw new io.evenly.core.shared.exception.ConflictException("Payment is settled and cannot be modified");
        }
    }

    private SettlementScope resolveSettlementScope(SettlementScope settlementScope, SettlementScope defaultScope) {
        return settlementScope != null ? settlementScope : defaultScope;
    }
}
