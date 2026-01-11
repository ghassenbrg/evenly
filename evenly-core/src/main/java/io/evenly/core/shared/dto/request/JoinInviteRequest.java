package io.evenly.core.shared.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * JoinInviteRequest DTO matching OpenAPI schema.
 */
public class JoinInviteRequest {
    @NotBlank(message = "Code is required")
    private String code;

    public JoinInviteRequest() {
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
