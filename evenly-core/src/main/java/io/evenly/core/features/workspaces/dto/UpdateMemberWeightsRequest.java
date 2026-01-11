package io.evenly.core.features.workspaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * UpdateMemberWeightsRequest DTO matching OpenAPI schema.
 */
public class UpdateMemberWeightsRequest {
    @NotEmpty(message = "Weights list cannot be empty")
    @Valid
    private List<MemberWeight> weights;

    public UpdateMemberWeightsRequest() {
    }

    // Getters and setters
    public List<MemberWeight> getWeights() {
        return weights;
    }

    public void setWeights(List<MemberWeight> weights) {
        this.weights = weights;
    }

    /**
     * MemberWeight nested class matching OpenAPI schema.
     */
    public static class MemberWeight {
        @jakarta.validation.constraints.NotBlank(message = "User ID is required")
        private String userId;
        
        @jakarta.validation.constraints.NotNull(message = "Weight percent is required")
        private Double weightPercent;
        
        private Double personalMonthlyLimit;

        public MemberWeight() {
        }

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Double getWeightPercent() {
            return weightPercent;
        }

        public void setWeightPercent(Double weightPercent) {
            this.weightPercent = weightPercent;
        }

        public Double getPersonalMonthlyLimit() {
            return personalMonthlyLimit;
        }

        public void setPersonalMonthlyLimit(Double personalMonthlyLimit) {
            this.personalMonthlyLimit = personalMonthlyLimit;
        }
    }
}
