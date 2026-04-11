package com.InformationModelingProjectManagementSystem.util;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.subsidiaryClasses.ChangePassword;

@Component
public class ChangePasswordValidator implements Validator {
    
    @Autowired
    public ChangePasswordValidator() {}

    @Override
    public boolean supports(Class<?> aClass) {
        return ChangePassword.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {       
    }

    public void validate(Object o, Person person, Errors errors) {
        ChangePassword passwords = (ChangePassword) o;         

        if(!passwords.checkLastPassword(person.getPassword()))
            errors.rejectValue("lastPassword", "", "Старый пароль введен неверно!");

        if(!(Pattern.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$", passwords.getNewPassword()))) 
            errors.rejectValue("newPassword", "", "Новый пароль должен состоять минимум из 8 символов и содержать хотя бы одну цифру, заглавную и строчную латинские буквы.");

        if(!((passwords.getNewPassword()).equals(passwords.getNewPasswordRepeat()))) 
            errors.rejectValue("newPasswordRepeat", "", "Введенные пароли не совпадают!");
    
    }
    
}
