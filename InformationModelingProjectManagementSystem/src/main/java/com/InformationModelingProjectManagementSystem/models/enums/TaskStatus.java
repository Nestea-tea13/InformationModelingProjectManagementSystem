package com.InformationModelingProjectManagementSystem.models.enums;

public enum TaskStatus {
    ASSIGNED("Назначено"),
    IN_PROGRESS("В работе"),
    REVIEW("На проверке"),
    COMPLETED("Выполнено"),
    REJECTED("Отклонено");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
