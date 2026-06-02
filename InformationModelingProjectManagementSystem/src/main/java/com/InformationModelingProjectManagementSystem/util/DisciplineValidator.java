package com.InformationModelingProjectManagementSystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Discipline;
import com.InformationModelingProjectManagementSystem.services.DisciplineService;

@Component
public class DisciplineValidator implements Validator {

    private final DisciplineService disciplineService;

    @Autowired
    public DisciplineValidator(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Discipline.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Discipline discipline = (Discipline) o;
        if (discipline.getShortName() == null) return;

        disciplineService.findByShortName(discipline.getShortName().trim())
            .ifPresent(existing -> {
                if (existing.getId() != discipline.getId()) {
                    errors.rejectValue("shortName", "", "Раздел с таким сокращением уже существует");
                }
            });
    }
    
}
