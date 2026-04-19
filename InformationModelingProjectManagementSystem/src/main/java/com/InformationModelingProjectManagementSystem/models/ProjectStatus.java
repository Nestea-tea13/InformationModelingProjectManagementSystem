package com.InformationModelingProjectManagementSystem.models;

public enum ProjectStatus {
    PLANNING("Планирование"),
    ACTIVE("Активный"),
    SUSPENDED("Приостановлен"),
    COMPLETED("Завершен"),
    CANCELLED("Отменен");
    
    private final String displayName;
    
    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
}
