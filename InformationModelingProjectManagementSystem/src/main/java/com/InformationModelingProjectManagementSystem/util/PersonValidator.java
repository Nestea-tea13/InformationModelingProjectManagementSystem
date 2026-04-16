package com.InformationModelingProjectManagementSystem.util;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.services.PeopleService;

@Component
public class PersonValidator implements Validator {

    private final PeopleService peopleService;

    @Autowired
    public PersonValidator(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Person.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Person person = (Person) o;

        Optional<Person> personFromDB = peopleService.findByEmail(person.getEmail());
        
        if (personFromDB.isPresent() && (person.getId() != personFromDB.get().getId()))
            errors.rejectValue("email", "", "Пользователь с такой почтой уже существует");

        if(person.getBirthday() == null || person.getBirthday().equals(""))
            errors.rejectValue("birthday", "", "Необходимо заполнить");

        if(person.getGender() == null)
            errors.rejectValue("gender", "", "Необходимо выбрать");

        if (person.getRole().equals("ROLE_USER") && person.getPosition() == null) {
            errors.rejectValue("position", "", "Необходимо выбрать должность");
        }

    }
    
}
