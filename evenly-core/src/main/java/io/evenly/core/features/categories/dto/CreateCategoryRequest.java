package io.evenly.core.features.categories.dto;

/**
 * CreateCategoryRequest DTO matching OpenAPI schema.
 */
public class CreateCategoryRequest {
    private String name;
    
    private String icon;
    
    private String color;

    public CreateCategoryRequest() {
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
