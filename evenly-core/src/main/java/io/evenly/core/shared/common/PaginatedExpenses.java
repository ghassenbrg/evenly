package io.evenly.core.shared.common;

import io.evenly.core.features.expenses.dto.Expense;
import java.util.List;

/**
 * PaginatedExpenses DTO matching OpenAPI schema.
 */
public class PaginatedExpenses {
    private List<Expense> data;
    private PageInfo page;
    private SortInfo sort;

    public PaginatedExpenses() {
    }

    public PaginatedExpenses(List<Expense> data, PageInfo page, SortInfo sort) {
        this.data = data;
        this.page = page;
        this.sort = sort;
    }

    // Getters and setters
    public List<Expense> getData() {
        return data;
    }

    public void setData(List<Expense> data) {
        this.data = data;
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }

    public SortInfo getSort() {
        return sort;
    }

    public void setSort(SortInfo sort) {
        this.sort = sort;
    }
}
