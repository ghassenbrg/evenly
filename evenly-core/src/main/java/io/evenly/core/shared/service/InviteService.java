package io.evenly.core.shared.service;

import io.evenly.core.shared.dto.Invite;
import io.evenly.core.shared.dto.request.CreateInviteRequest;

/**
 * Service interface for invite operations.
 * Designed to be swappable with real persistence implementations.
 */
public interface InviteService {
    Invite create(String workspaceId, CreateInviteRequest request);
    void joinWorkspace(String code, String userId);
}
