package io.evenly.core.features.settlements.dto;

public class SettlementStatus {
    private boolean canSuggest;
    private boolean hasUnsettled;

    public SettlementStatus() {
    }

    public boolean isCanSuggest() {
        return canSuggest;
    }

    public void setCanSuggest(boolean canSuggest) {
        this.canSuggest = canSuggest;
    }

    public boolean isHasUnsettled() {
        return hasUnsettled;
    }

    public void setHasUnsettled(boolean hasUnsettled) {
        this.hasUnsettled = hasUnsettled;
    }
}
