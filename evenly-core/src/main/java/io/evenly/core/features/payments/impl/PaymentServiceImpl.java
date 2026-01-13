package io.evenly.core.features.payments.impl;

import io.evenly.core.domain.repository.PaymentRepository;
import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.domain.repository.WorkspaceRepository;
import io.evenly.core.features.payments.PaymentService;
import io.evenly.core.features.payments.dto.CreatePaymentRequest;
import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.payments.dto.UpdatePaymentRequest;
import io.evenly.core.shared.common.PageInfo;
import io.evenly.core.shared.common.PaginatedPayments;
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

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedPayments findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
                                             String status, int page, int size, String sort) {
        UUID workspaceUuid = UUID.fromString(workspaceId);

        List<io.evenly.core.domain.Payment> domainPayments = paymentRepository.findByWorkspaceId(
            workspaceUuid, startDate, endDate, status, page, size, sort);

        long total = paymentRepository.countByWorkspaceId(workspaceUuid);

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
        return toDto(payment);
    }

    @Override
    @Transactional
    public io.evenly.core.features.payments.dto.Payment update(String paymentId, UpdatePaymentRequest request) {
        UUID paymentUuid = UUID.fromString(paymentId);
        io.evenly.core.domain.Payment payment = paymentRepository.findById(paymentUuid)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

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
        return toDto(payment);
    }

    @Override
    @Transactional
    public void delete(String paymentId) {
        UUID paymentUuid = UUID.fromString(paymentId);
        paymentRepository.delete(paymentUuid);
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

        userRepository.findById(domain.getPaidByUserId()).ifPresent((io.evenly.core.domain.User user) -> { // userId is now String
            dto.setPaidByUserName(user.getDisplayName());
        });

        userRepository.findById(domain.getPayeeUserId()).ifPresent((io.evenly.core.domain.User user) -> { // userId is now String
            dto.setPayeeUserName(user.getDisplayName());
        });

        return dto;
    }
}
