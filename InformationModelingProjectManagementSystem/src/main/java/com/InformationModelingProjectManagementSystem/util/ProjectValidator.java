package com.InformationModelingProjectManagementSystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.services.ProjectService;

@Component
public class ProjectValidator implements Validator {
    
    private final ProjectService projectService;
    
    @Autowired
    public ProjectValidator(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    @Override
    public boolean supports(Class<?> aClass) {
        return Project.class.equals(aClass);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        Project project = (Project) target;
        
        // Проверка уникальности кода проекта
        if (project.getCode() != null && !project.getCode().isEmpty()) {
            projectService.findByCode(project.getCode())
                .ifPresent(existingProject -> {
                    if (existingProject.getId() != project.getId()) {
                        errors.rejectValue("code", "", "Проект с таким кодом уже существует");
                    }
                });
        }
        
        // Проверка дат: дата окончания не может быть раньше даты начала
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEndDate().isBefore(project.getStartDate())) {
                errors.rejectValue("endDate", "", "Дата окончания не может быть раньше даты начала");
            }
        }
    }
}