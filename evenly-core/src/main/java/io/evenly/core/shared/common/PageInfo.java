package io.evenly.core.shared.common;

/**
 * PageInfo DTO matching OpenAPI schema.
 */
public class PageInfo {
    private Integer number;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;

    public PageInfo() {
    }

    public PageInfo(Integer number, Integer size, Integer totalElements, Integer totalPages) {
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getters and setters
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
