package io.evenly.core.features.notifications.dto;

import java.time.OffsetDateTime;

/**
 * Notification DTO matching OpenAPI schema.
 */
public class Notification {
    private String id;
    private String type;
    private String content;
    private OffsetDateTime timestamp;
    private String workspaceId;
    private Boolean read;

    public Notification() {
    }

    public Notification(String id, String type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
