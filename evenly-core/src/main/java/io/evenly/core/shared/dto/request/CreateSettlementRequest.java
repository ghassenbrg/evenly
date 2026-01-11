package io.evenly.core.shared.dto.request;

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
