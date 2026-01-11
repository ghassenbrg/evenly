package io.evenly.core.features.analytics.dto;

import java.util.List;

/**
 * ExpenseSnapshotResponse DTO matching OpenAPI schema.
 */
public class ExpenseSnapshotResponse {
    private List<ExpenseSnapshotItem> data;
    private Integer categoriesCount;
    private Integer remainingCategoriesCount;

    public ExpenseSnapshotResponse() {
    }

    public ExpenseSnapshotResponse(List<ExpenseSnapshotItem> data, Integer categoriesCount, Integer remainingCategoriesCount) {
        this.data = data;
        this.categoriesCount = categoriesCount;
        this.remainingCategoriesCount = remainingCategoriesCount;
    }

    // Getters and setters
    public List<ExpenseSnapshotItem> getData() {
        return data;
    }

    public void setData(List<ExpenseSnapshotItem> data) {
        this.data = data;
    }

    public Integer getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(Integer categoriesCount) {
        this.categoriesCount = categoriesCount;
    }

    public Integer getRemainingCategoriesCount() {
        return remainingCategoriesCount;
    }

    public void setRemainingCategoriesCount(Integer remainingCategoriesCount) {
        this.remainingCategoriesCount = remainingCategoriesCount;
    }
}
