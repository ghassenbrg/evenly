package io.evenly.core.shared.common;

import io.evenly.core.features.payments.dto.Payment;
import java.util.List;

/**
 * PaginatedPayments DTO matching OpenAPI schema.
 */
public class PaginatedPayments {
    private List<Payment> data;
    private PageInfo page;
    private SortInfo sort;

    public PaginatedPayments() {
    }

    public PaginatedPayments(List<Payment> data, PageInfo page, SortInfo sort) {
        this.data = data;
        this.page = page;
        this.sort = sort;
    }

    // Getters and setters
    public List<Payment> getData() {
        return data;
    }

    public void setData(List<Payment> data) {
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
