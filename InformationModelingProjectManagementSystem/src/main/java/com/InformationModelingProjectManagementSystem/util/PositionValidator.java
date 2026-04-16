package com.InformationModelingProjectManagementSystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Position;
import com.InformationModelingProjectManagementSystem.services.PositionService;

@Component
public class PositionValidator implements Validator {
    
    private final PositionService positionService;
    
    @Autowired
    public PositionValidator(PositionService positionService) {
        this.positionService = positionService;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return Position.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        Position position = (Position) target;
        
        positionService.findByName(position.getName())
            .ifPresent(existingPosition -> {
                if (existingPosition.getId() != position.getId()) {
                    errors.rejectValue("name", "", "Должность с таким названием уже существует");
                }
            });
        
        if (position.getCanAssignTo() != null && position.getCanAssignTo().contains(position)) {
            errors.rejectValue("canAssignTo", "", "Должность не может быть подчиненной самой себя");
        }
    }
}
