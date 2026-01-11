package io.evenly.core.features.settlements.dto;

/**
 * CreateSettlementRequest DTO matching OpenAPI schema.
 */
public class CreateSettlementRequest {
    private String note;

    public CreateSettlementRequest() {
    }

    // Getters and setters
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
