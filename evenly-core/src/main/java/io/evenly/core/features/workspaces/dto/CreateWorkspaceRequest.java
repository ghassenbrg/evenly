package io.evenly.core.features.workspaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * CreateWorkspaceRequest DTO matching OpenAPI schema.
 */
public class CreateWorkspaceRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Default split mode is required")
    @Pattern(regexp = "EQUAL|WEIGHTED", message = "Default split mode must be EQUAL or WEIGHTED")
    private String defaultSplitMode;
    
    private Double monthlySharedLimit;
    private String currency;

    public CreateWorkspaceRequest() {
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
