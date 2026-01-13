package io.evenly.core.features.invites.impl;

import io.evenly.core.domain.WorkspaceMember;
import io.evenly.core.domain.repository.InviteRepository;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.domain.repository.WorkspaceRepository;
import io.evenly.core.features.invites.dto.CreateInviteRequest;
import io.evenly.core.features.invites.dto.Invite;
import io.evenly.core.shared.exception.ConflictException;
import io.evenly.core.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class InviteServiceImpl implements io.evenly.core.features.invites.InviteService {

    @Inject
    private InviteRepository inviteRepository;

    @Inject
    private WorkspaceRepository workspaceRepository;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional
    public Invite create(String workspaceId, CreateInviteRequest request) {
        UUID workspaceUuid = UUID.fromString(workspaceId);
        
        workspaceRepository.findById(workspaceUuid)
            .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));

        // Generate unique code
        String code = generateUniqueCode();

        // Set default values: maxUses = 5, expiresInDays = 2
        // If maxUses is null or <= 0, use default of 5
        Integer maxUses = (request.getMaxUses() != null && request.getMaxUses() > 0) 
            ? request.getMaxUses() 
            : 5;
        // If expiresInDays is null or <= 0, use default of 2
        Integer expiresInDays = (request.getExpiresInDays() != null && request.getExpiresInDays() > 0) 
            ? request.getExpiresInDays() 
            : 2;

        // Ensure maxUses is never null, 0, or negative (safety check)
        if (maxUses == null || maxUses <= 0) {
            maxUses = 5;
        }

        // Calculate expiration date from expiresInDays
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(expiresInDays);

        io.evenly.core.domain.Invite domainInvite = io.evenly.core.domain.Invite.builder()
            .workspaceId(workspaceUuid)
            .code(code)
            .expiresAt(expiresAt)
            .maxUses(maxUses)
            .usesCount(0)
            .createdAt(OffsetDateTime.now())
            .build();

        domainInvite = inviteRepository.save(domainInvite);
        return toDto(domainInvite);
    }

    @Override
    @Transactional
    public void joinWorkspace(String code, String userId) { // userId is now username (String)
        io.evenly.core.domain.Invite domainInvite = inviteRepository.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Invite not found: " + code));

        // Check if invite is expired
        if (domainInvite.getExpiresAt() != null && domainInvite.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ConflictException("Invite has expired");
        }

        // Check if max uses reached
        if (domainInvite.getMaxUses() != null && domainInvite.getUsesCount() >= domainInvite.getMaxUses()) {
            throw new ConflictException("Invite has reached maximum uses");
        }

        // Check if user is already a member
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(domainInvite.getWorkspaceId(), userId)) { // userId is now String
            throw new ConflictException("User is already a member of this workspace");
        }

        // Add user to workspace
        WorkspaceMember member = WorkspaceMember.builder()
            .workspaceId(domainInvite.getWorkspaceId())
            .userId(userId) // userId is now username (String)
            .role("MEMBER")
            .build();
        workspaceMemberRepository.save(member);

        // Increment used count
        domainInvite.setUsesCount(domainInvite.getUsesCount() + 1);
        inviteRepository.save(domainInvite);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    private String generateUniqueCode() {
        // Simple implementation - generate a random code
        // In production, you might want a more sophisticated approach
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (inviteRepository.existsByCode(code));
        return code;
    }

    private Invite toDto(io.evenly.core.domain.Invite domain) {
        Invite dto = new Invite();
        dto.setId(domain.getId().toString());
        dto.setWorkspaceId(domain.getWorkspaceId().toString());
        dto.setCode(domain.getCode());
        dto.setExpiresAt(domain.getExpiresAt());
        dto.setMaxUses(domain.getMaxUses());
        dto.setUsesCount(domain.getUsesCount());
        dto.setCreatedAt(domain.getCreatedAt());
        return dto;
    }
}
