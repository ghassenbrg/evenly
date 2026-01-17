package io.evenly.core.features.expenses.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.evenly.core.domain.ExpenseParticipant;
import io.evenly.core.domain.repository.CategoryRepository;
import io.evenly.core.domain.repository.ExpenseParticipantRepository;
import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.domain.repository.UserRepository;
import io.evenly.core.domain.NotificationType;
import io.evenly.core.features.expenses.dto.CreateExpenseRequest;
import io.evenly.core.features.expenses.dto.UpdateExpenseRequest;
import io.evenly.core.features.notifications.NotificationService;
import io.evenly.core.shared.common.PageInfo;
import io.evenly.core.shared.common.PaginatedExpenses;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ExpenseServiceImpl implements io.evenly.core.features.expenses.ExpenseService {

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private ExpenseParticipantRepository expenseParticipantRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceRepository workspaceRepository;

    @Inject
    private io.evenly.core.domain.repository.WorkspaceMemberRepository workspaceMemberRepository;

    @Inject
    private NotificationService notificationService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedExpenses findForWorkspace(String workspaceId, LocalDate startDate, LocalDate endDate,
            String categoryId, String status, int page, int size, String sort) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        UUID categoryUuid = categoryId != null ? UUID.fromString(categoryId) : null;
        Boolean settledFilter = resolveSettledFilter(status);

        List<io.evenly.core.domain.Expense> domainExpenses = expenseRepository.findByWorkspaceId(
                workspaceUuid, startDate, endDate, categoryUuid, settledFilter, page, size, sort);

        long total = expenseRepository.countByWorkspaceIdAndDateRange(workspaceUuid, startDate, endDate, settledFilter);

        List<io.evenly.core.features.expenses.dto.Expense> expenseDtos = domainExpenses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginatedExpenses result = new PaginatedExpenses();
        result.setData(expenseDtos);
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
    public List<io.evenly.core.features.expenses.dto.Expense> findRecentForWorkspace(String workspaceId, int size) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        List<io.evenly.core.domain.Expense> domainExpenses = expenseRepository.findByWorkspaceId(
                workspaceUuid, null, null, null, false, 0, size, "effectiveDate,DESC");

        return domainExpenses.stream()
                .limit(size)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<io.evenly.core.features.expenses.dto.Expense> findById(String expenseId) {
        UUID expenseUuid = UUID.fromString(expenseId);
        return expenseRepository.findById(expenseUuid)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public io.evenly.core.features.expenses.dto.Expense create(String workspaceId, String userId,
            CreateExpenseRequest request) { // userId is now username (String)
        UUID workspaceUuid = UUID.fromString(workspaceId);

        // Get workspace to get currency
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
                .orElseThrow(() -> new io.evenly.core.shared.exception.NotFoundException(
                        "Workspace not found: " + workspaceId));

        io.evenly.core.domain.Expense expense = io.evenly.core.domain.Expense.builder()
                .workspaceId(workspaceUuid)
                .categoryId(request.getCategoryId() != null ? UUID.fromString(request.getCategoryId()) : null)
                .amount(request.getAmount())
                .currency(workspace.getCurrency()) // Use workspace currency (enum)
                .effectiveDate(request.getDate() != null ? request.getDate() : java.time.LocalDate.now())
                .note(request.getNote())
                .paidByUserId(userId) // userId is now username (String)
                .createdByUserId(userId) // userId is now username (String)
                .build();

        expense = expenseRepository.save(expense);

        // Handle participants - if none specified, add all workspace members
        List<String> participantIds;
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            participantIds = request.getParticipantIds(); // participantIds are now usernames (String)
        } else {
            // Get all workspace members if empty
            participantIds = workspaceMemberRepository.findByWorkspaceId(workspaceUuid).stream()
                    .map(io.evenly.core.domain.WorkspaceMember::getUserId)
                    .collect(Collectors.toList());
        }

        for (String participantId : participantIds) {
            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expenseId(expense.getId())
                    .userId(participantId) // userId is now username (String)
                    .build();
            expenseParticipantRepository.save(participant);
        }

        notificationService.notifyExpenseEvent(workspaceId, userId, expense.getId().toString(),
            participantIds, NotificationType.EXPENSE_CREATED, buildExpenseDetail(expense));
        checkBudgetThresholds(workspaceId, userId, expense.getEffectiveDate());

        return toDto(expense);
    }

    @Override
    @Transactional
    public io.evenly.core.features.expenses.dto.Expense update(String expenseId, String userId, UpdateExpenseRequest request) {
        UUID expenseUuid = UUID.fromString(expenseId);
        io.evenly.core.domain.Expense expense = expenseRepository.findById(expenseUuid)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + expenseId));
        ensureNotSettled(expense);

        if (request.getCategoryId() != null) {
            expense.setCategoryId(UUID.fromString(request.getCategoryId()));
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        // Currency comes from workspace, not request
        if (request.getDate() != null) {
            expense.setEffectiveDate(request.getDate());
        }
        if (request.getNote() != null) {
            expense.setNote(request.getNote());
        }

        expense = expenseRepository.save(expense);

        List<String> participantIds = expenseParticipantRepository.findByExpenseId(expenseUuid).stream()
            .map(ExpenseParticipant::getUserId)
            .collect(Collectors.toList());

        notificationService.notifyExpenseEvent(expense.getWorkspaceId().toString(), userId, expenseId,
            participantIds, NotificationType.EXPENSE_UPDATED, buildExpenseDetail(expense));
        checkBudgetThresholds(expense.getWorkspaceId().toString(), userId, expense.getEffectiveDate());

        return toDto(expense);
    }

    @Override
    @Transactional
    public void delete(String expenseId, String userId) {
        UUID expenseUuid = UUID.fromString(expenseId);
        io.evenly.core.domain.Expense expense = expenseRepository.findById(expenseUuid)
            .orElseThrow(() -> new NotFoundException("Expense not found: " + expenseId));
        ensureNotSettled(expense);
        List<String> participantIds = expenseParticipantRepository.findByExpenseId(expenseUuid).stream()
            .map(ExpenseParticipant::getUserId)
            .collect(Collectors.toList());

        expenseParticipantRepository.deleteByExpenseId(expenseUuid);
        expenseRepository.delete(expenseUuid);

        notificationService.notifyExpenseEvent(expense.getWorkspaceId().toString(), userId, expenseId,
            participantIds, NotificationType.EXPENSE_DELETED, buildExpenseDetail(expense));
        checkBudgetThresholds(expense.getWorkspaceId().toString(), userId, expense.getEffectiveDate());
    }

    private void checkBudgetThresholds(String workspaceId, String userId, LocalDate effectiveDate) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        io.evenly.core.domain.Workspace workspace = workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));

        LocalDate date = effectiveDate != null ? effectiveDate : LocalDate.now();
        java.time.YearMonth month = java.time.YearMonth.from(date);
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<io.evenly.core.domain.Expense> expenses = expenseRepository.findByWorkspaceId(
            workspaceUuid, startDate, endDate, null, false, 0, Integer.MAX_VALUE, null);
        java.math.BigDecimal workspaceTotalPaid = expenses.stream()
            .map(io.evenly.core.domain.Expense::getAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        notificationService.checkBudgetThresholds(workspaceId, userId, date, workspaceTotalPaid,
            workspace.getMonthlySharedLimit());
    }

    private String buildExpenseDetail(io.evenly.core.domain.Expense expense) {
        if (expense == null) {
            return "Expense updated";
        }
        String amount = expense.getAmount() != null ? expense.getAmount().stripTrailingZeros().toPlainString() : null;
        String currency = expense.getCurrency() != null ? expense.getCurrency().getCode() : null;
        StringBuilder detail = new StringBuilder();
        if (amount != null) {
            detail.append("Expense of ").append(amount);
            if (currency != null) {
                detail.append(" ").append(currency);
            }
        } else {
            detail.append("Expense updated");
        }

        if (expense.getCategoryId() != null) {
            categoryRepository.findById(expense.getCategoryId()).ifPresent(category -> {
                detail.append(" for ").append(category.getName());
            });
        }
        return detail.toString();
    }

    private io.evenly.core.features.expenses.dto.Expense toDto(io.evenly.core.domain.Expense domain) {
        io.evenly.core.features.expenses.dto.Expense dto = new io.evenly.core.features.expenses.dto.Expense();
        dto.setId(domain.getId().toString());
        dto.setAmount(domain.getAmount());
        dto.setCurrency(domain.getCurrency() != null ? domain.getCurrency().getCode() : null);
        dto.setEffectiveDate(domain.getEffectiveDate());
        dto.setNote(domain.getNote());
        dto.setPaidByUserId(domain.getPaidByUserId()); // userId is now String, no need to convert
        dto.setStatus(domain.getSettlementId() == null ? "ACTIVE" : "SETTLED");
        dto.setSettlementId(domain.getSettlementId() != null ? domain.getSettlementId().toString() : null);
        dto.setSettledAt(domain.getSettledAt());

        if (domain.getCategoryId() != null) {
            dto.setCategoryId(domain.getCategoryId().toString());
            categoryRepository.findById(domain.getCategoryId()).ifPresent(category -> {
                dto.setCategoryName(category.getName());
                dto.setCategoryIcon(category.getIcon());
                dto.setCategoryColor(category.getColor());
            });
        }

        userRepository.findById(domain.getPaidByUserId()).ifPresent(user -> { // userId is now String
            dto.setPaidByUserName(user.getDisplayName());
        });

        return dto;
    }

    private void ensureNotSettled(io.evenly.core.domain.Expense expense) {
        if (expense.getSettlementId() != null) {
            throw new io.evenly.core.shared.exception.ConflictException("Expense is settled and cannot be modified");
        }
    }

    private Boolean resolveSettledFilter(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return Boolean.FALSE;
        }
        if ("SETTLED".equalsIgnoreCase(status)) {
            return Boolean.TRUE;
        }
        return null;
    }
}
