package io.evenly.core.features.workspaces.impl;

import io.evenly.core.domain.Workspace;
import io.evenly.core.domain.WorkspaceMember;
import io.evenly.core.domain.Invite;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.domain.repository.WorkspaceRepository;
import io.evenly.core.domain.repository.InviteRepository;
import io.evenly.core.features.workspaces.dto.CreateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceRequest;
import io.evenly.core.features.workspaces.dto.UpdateWorkspaceSettingsRequest;
import io.evenly.core.features.workspaces.dto.UpdateMemberWeightsRequest;
import io.evenly.core.features.notifications.NotificationService;
import io.evenly.core.shared.exception.ConflictException;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Service implementation for workspace operations.
 * Uses repositories (ports) and enforces business rules.
 * Works with both mock and PostgreSQL repositories transparently.
 */
@ApplicationScoped
public class WorkspaceServiceImpl implements io.evenly.core.features.workspaces.WorkspaceService {

    @Inject
    private WorkspaceRepository workspaceRepository;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Inject
    private InviteRepository inviteRepository;

    @Inject
    private NotificationService notificationService;

    @Override
    public List<io.evenly.core.features.workspaces.dto.Workspace> findAllForUser(String userId) { // userId is now username (String)
        List<Workspace> workspaces = workspaceRepository.findByUserId(userId);
        return workspaces.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<io.evenly.core.features.workspaces.dto.Workspace> findById(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        return workspaceRepository.findById(workspaceUuid)
            .map(this::toDto);
    }

    @Override
    @Transactional
    public io.evenly.core.features.workspaces.dto.Workspace create(String userId, CreateWorkspaceRequest request) { // userId is now username (String)
        // Business rule: Validate split mode
        if (!"EQUAL".equals(request.getDefaultSplitMode()) && !"WEIGHTED".equals(request.getDefaultSplitMode())) {
            throw new IllegalArgumentException("Invalid split mode: " + request.getDefaultSplitMode());
        }

        // Business rule: Validate monthly limit if provided
        BigDecimal monthlyLimit = request.getMonthlySharedLimit() != null 
            ? BigDecimal.valueOf(request.getMonthlySharedLimit()) 
            : null;
        if (monthlyLimit != null && monthlyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly shared limit must be positive");
        }

        UUID workspaceUuid = UUID.randomUUID();

        // Create workspace
        Workspace workspace = Workspace.builder()
            .id(workspaceUuid)
            .name(request.getName())
            .defaultSplitMode(request.getDefaultSplitMode())
            .monthlySharedLimit(monthlyLimit)
            .isPersonal(false)
            .currency(request.getCurrency() != null 
                ? io.evenly.core.features.currencies.SupportedCurrency.findByCode(request.getCurrency()) 
                : io.evenly.core.features.currencies.SupportedCurrency.USD)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        workspace = workspaceRepository.save(workspace);

        // Business rule: Add creator as OWNER member
        WorkspaceMember ownerMember = WorkspaceMember.builder()
            .workspaceId(workspaceUuid)
            .userId(userId) // userId is now username (String)
            .role("OWNER")
            .weightPercent(new BigDecimal("100.00"))
            .personalMonthlyLimit(null)
            .joinedAt(OffsetDateTime.now())
            .build();

        workspaceMemberRepository.save(ownerMember);

        return toDto(workspace);
    }

    @Override
    @Transactional
    public io.evenly.core.features.workspaces.dto.Workspace update(String workspaceId, String userId, UpdateWorkspaceRequest request) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        Workspace workspace = workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));

        // Convert Double to BigDecimal for monthly limit
        BigDecimal monthlyLimit = request.getMonthlySharedLimit() != null 
            ? BigDecimal.valueOf(request.getMonthlySharedLimit()) 
            : workspace.getMonthlySharedLimit();

        // Business rule: Validate split mode if provided
        if (request.getDefaultSplitMode() != null) {
            if (!"EQUAL".equals(request.getDefaultSplitMode()) && !"WEIGHTED".equals(request.getDefaultSplitMode())) {
                throw new IllegalArgumentException("Invalid split mode: " + request.getDefaultSplitMode());
            }
        }

        // Business rule: Validate monthly limit if provided
        if (monthlyLimit != null && monthlyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly shared limit must be positive");
        }

        // Update the existing entity directly (keeps it managed by EntityManager)
        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDefaultSplitMode() != null) {
            workspace.setDefaultSplitMode(request.getDefaultSplitMode());
        }
        workspace.setMonthlySharedLimit(monthlyLimit);
        workspace.setUpdatedAt(OffsetDateTime.now());

        workspace = workspaceRepository.save(workspace);
        notificationService.notifyWorkspaceUpdated(workspaceId, userId, "Workspace settings updated");
        return toDto(workspace);
    }

    @Override
    @Transactional
    public void delete(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));

        // Business rule: Cannot delete workspace with expenses or payments
        if (workspaceRepository.hasExpenses(workspaceUuid)) {
            throw new ConflictException("Cannot delete workspace with active expenses");
        }
        if (workspaceRepository.hasPayments(workspaceUuid)) {
            throw new ConflictException("Cannot delete workspace with active payments");
        }

        workspaceRepository.delete(workspaceUuid);
    }

    @Override
    @Transactional
    public io.evenly.core.features.workspaces.dto.Workspace updateSettings(String workspaceId, String userId, UpdateWorkspaceSettingsRequest request) {
        UpdateWorkspaceRequest updateRequest = new UpdateWorkspaceRequest();
        updateRequest.setName(request.getName());
        updateRequest.setDefaultSplitMode(request.getDefaultSplitMode());
        updateRequest.setMonthlySharedLimit(request.getMonthlySharedLimit());
        return update(workspaceId, userId, updateRequest);
    }

    @Override
    public List<io.evenly.core.features.workspaces.dto.WorkspaceMember> findMembers(String workspaceId) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));

        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceUuid);
        return members.stream()
            .map(this::memberToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public io.evenly.core.features.workspaces.dto.Workspace createPersonalWorkspace(String userId, String currency) { // userId is now username (String)
        UUID workspaceUuid = UUID.randomUUID();
        
        // Create personal workspace
        Workspace workspace = Workspace.builder()
            .id(workspaceUuid)
            .name("Personal")
            .defaultSplitMode("EQUAL")
            .monthlySharedLimit(null)
            .isPersonal(true)
            .currency(currency != null 
                ? io.evenly.core.features.currencies.SupportedCurrency.findByCode(currency) 
                : io.evenly.core.features.currencies.SupportedCurrency.USD)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        workspace = workspaceRepository.save(workspace);

        // Add user as OWNER member
        WorkspaceMember ownerMember = WorkspaceMember.builder()
            .workspaceId(workspaceUuid)
            .userId(userId) // userId is now username (String)
            .role("OWNER")
            .weightPercent(new BigDecimal("100.00"))
            .personalMonthlyLimit(null)
            .joinedAt(OffsetDateTime.now())
            .build();

        workspaceMemberRepository.save(ownerMember);

        return toDto(workspace);
    }

    @Override
    @Transactional
    public void updateMemberWeights(String workspaceId, String userId, UpdateMemberWeightsRequest request) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));
        
        // Business rule: Validate weight percentages sum to 100
        BigDecimal totalWeight = request.getWeights().stream()
            .map(w -> BigDecimal.valueOf(w.getWeightPercent()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Weight percentages must sum to 100");
        }

        // Update member weights
        for (UpdateMemberWeightsRequest.MemberWeight weight : request.getWeights()) {
            String weightUserId = weight.getUserId(); // userId is now username (String)
            WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceUuid, weightUserId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + weight.getUserId()));

            BigDecimal weightPercent = BigDecimal.valueOf(weight.getWeightPercent());
            BigDecimal personalLimit = weight.getPersonalMonthlyLimit() != null 
                ? BigDecimal.valueOf(weight.getPersonalMonthlyLimit()) 
                : null;
            
            // Update the existing entity directly (keeps it managed by EntityManager)
            member.setWeightPercent(weightPercent);
            member.setPersonalMonthlyLimit(personalLimit);

            workspaceMemberRepository.save(member);
        }

        notificationService.notifyWorkspaceUpdated(workspaceId, userId, "Member weights updated");
    }

    private io.evenly.core.features.workspaces.dto.Workspace toDto(Workspace workspace) {
        io.evenly.core.features.workspaces.dto.Workspace dto = new io.evenly.core.features.workspaces.dto.Workspace();
        dto.setId(workspace.getId().toString());
        dto.setName(workspace.getName());
        dto.setDefaultSplitMode(workspace.getDefaultSplitMode());
        dto.setMonthlySharedLimit(workspace.getMonthlySharedLimit() != null ? workspace.getMonthlySharedLimit().doubleValue() : null);
        dto.setIsPersonal(workspace.getIsPersonal());
        dto.setCurrency(workspace.getCurrency() != null ? workspace.getCurrency().getCode() : null);
        dto.setCreatedAt(workspace.getCreatedAt());
        dto.setUpdatedAt(workspace.getUpdatedAt());
        
        // Get the most recent active invite for this workspace
        List<Invite> invites = inviteRepository.findByWorkspaceId(workspace.getId());
        Optional<Invite> activeInvite = invites.stream()
            .filter(invite -> {
                // Check if invite is not expired
                if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
                    return false;
                }
                // Check if invite hasn't reached max uses
                if (invite.getMaxUses() != null && invite.getUsesCount() >= invite.getMaxUses()) {
                    return false;
                }
                return true;
            })
            .max(Comparator.comparing(Invite::getCreatedAt));
        
        if (activeInvite.isPresent()) {
            String code = activeInvite.get().getCode();
            dto.setInviteCode(code);
            // Generate invite link - using localhost:3000 as base URL (can be made configurable)
            dto.setInviteLink("https://localhost:3000/workspace/join/" + code);
        } else {
            dto.setInviteCode(null);
            dto.setInviteLink(null);
        }
        
        return dto;
    }

    private io.evenly.core.features.workspaces.dto.WorkspaceMember memberToDto(WorkspaceMember member) {
        io.evenly.core.features.workspaces.dto.WorkspaceMember dto = new io.evenly.core.features.workspaces.dto.WorkspaceMember();
        dto.setUserId(member.getUserId()); // userId is now String, no need to convert
        dto.setRole(member.getRole());
        dto.setWeightPercent(member.getWeightPercent() != null ? member.getWeightPercent().doubleValue() : null);
        dto.setPersonalMonthlyLimit(member.getPersonalMonthlyLimit() != null ? member.getPersonalMonthlyLimit().doubleValue() : null);
        return dto;
    }
}
