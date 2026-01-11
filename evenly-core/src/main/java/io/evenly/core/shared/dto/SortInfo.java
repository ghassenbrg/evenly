package io.evenly.core.shared.dto;

/**
 * SortInfo DTO matching OpenAPI schema.
 */
public class SortInfo {
    private Boolean sorted;
    private String direction; // ASC, DESC
    private String property;

    public SortInfo() {
    }

    public SortInfo(Boolean sorted, String direction, String property) {
        this.sorted = sorted;
        this.direction = direction;
        this.property = property;
    }

    // Getters and setters
    public Boolean getSorted() {
        return sorted;
    }

    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
