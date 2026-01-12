package io.evenly.core.domain;

import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

/**
 * Composite primary key for WorkspaceMember entity.
 */
public class WorkspaceMemberId implements Serializable {
    private UUID workspaceId;
    private String userId; // Changed to String (username)

    public WorkspaceMemberId() {
    }

    public WorkspaceMemberId(UUID workspaceId, String userId) {
        this.workspaceId = workspaceId;
        this.userId = userId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceMemberId that = (WorkspaceMemberId) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, userId);
    }
}
