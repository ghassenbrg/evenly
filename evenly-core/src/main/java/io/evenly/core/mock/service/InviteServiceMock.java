package io.evenly.core.mock.service;

import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import io.evenly.core.features.invites.dto.Invite;
import io.evenly.core.features.invites.dto.CreateInviteRequest;
import io.evenly.core.features.invites.InviteService;

/**
 \1
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class InviteServiceMock implements InviteService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public Invite create(String workspaceId, CreateInviteRequest request) {
        Invite invite = new Invite();
        invite.setId(workspaceId + "-inv-" + UUID.randomUUID().toString().substring(0, 8));
        invite.setWorkspaceId(workspaceId);
        invite.setCode(generateInviteCode());
        invite.setMaxUses(request.getMaxUses());
        invite.setUsesCount(0);
        
        int expiresInDays = request.getExpiresInDays() != null ? request.getExpiresInDays() : 30;
        invite.setExpiresAt(OffsetDateTime.now().plusDays(expiresInDays));
        invite.setCreatedAt(OffsetDateTime.now());
        
        mockDataProvider.getWorkspaceInvites().computeIfAbsent(workspaceId, k -> new ArrayList<>()).add(invite);
        return invite;
    }
    
    @Override
    public void joinWorkspace(String code, String userId) {
        // Find invite by code
        Invite invite = mockDataProvider.getWorkspaceInvites().values().stream()
            .flatMap(List::stream)
            .filter(inv -> code.equals(inv.getCode()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Invalid invite code"));
        
        // Check if expired
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Invite code has expired");
        }
        
        // Check if max uses reached
        if (invite.getUsesCount() >= invite.getMaxUses()) {
            throw new RuntimeException("Invite code has reached maximum uses");
        }
        
        // Increment uses
        invite.setUsesCount(invite.getUsesCount() + 1);
        
        // Add user to workspace (simplified - in real implementation would add to workspace members)
        // This is handled by workspace service in real implementation
    }
    
    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
