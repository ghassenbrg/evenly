package io.evenly.core.features.invites;

import io.evenly.core.features.invites.dto.Invite;
import io.evenly.core.features.invites.dto.CreateInviteRequest;

/**
 * Service interface for invite operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface InviteService {
    Invite create(String workspaceId, CreateInviteRequest request);
    void joinWorkspace(String code, String userId);
}
