package io.evenly.core.features.analytics.dto;

import java.math.BigDecimal;

/**
 * ExpenseSnapshotItem DTO matching OpenAPI schema.
 */
public class ExpenseSnapshotItem {
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal totalAmount;
    private BigDecimal spentPercentage;
    private Integer expensesCount;

    public ExpenseSnapshotItem() {
    }

    // Getters and setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getSpentPercentage() {
        return spentPercentage;
    }

    public void setSpentPercentage(BigDecimal spentPercentage) {
        this.spentPercentage = spentPercentage;
    }

    public Integer getExpensesCount() {
        return expensesCount;
    }

    public void setExpensesCount(Integer expensesCount) {
        this.expensesCount = expensesCount;
    }
}
