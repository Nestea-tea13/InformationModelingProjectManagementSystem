package com.InformationModelingProjectManagementSystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.services.ProjectService;
import com.InformationModelingProjectManagementSystem.services.TaskService;

@Component
public class TaskValidator implements Validator {

    private final TaskService taskService;
    private final ProjectService projectService;

    @Autowired
    public TaskValidator(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Task.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Task task = (Task) target;

        if (task.getDeadline() != null && task.getDeadline().isBefore(java.time.LocalDate.now())) {
            errors.rejectValue("deadline", "", "Срок выполнения не может быть раньше сегодняшнего дня");
        }
    }

}