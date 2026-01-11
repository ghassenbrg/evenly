package io.evenly.core.shared.dto.request;

import jakarta.validation.constraints.Pattern;

/**
 * UpdateWorkspaceRequest DTO matching OpenAPI schema.
 */
public class UpdateWorkspaceRequest {
    private String name;
    
    @Pattern(regexp = "EQUAL|WEIGHTED", message = "Default split mode must be EQUAL or WEIGHTED")
    private String defaultSplitMode;
    
    private Double monthlySharedLimit;

    public UpdateWorkspaceRequest() {
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultSplitMode() {
        return defaultSplitMode;
    }

    public void setDefaultSplitMode(String defaultSplitMode) {
        this.defaultSplitMode = defaultSplitMode;
    }

    public Double getMonthlySharedLimit() {
        return monthlySharedLimit;
    }

    public void setMonthlySharedLimit(Double monthlySharedLimit) {
        this.monthlySharedLimit = monthlySharedLimit;
    }
}
