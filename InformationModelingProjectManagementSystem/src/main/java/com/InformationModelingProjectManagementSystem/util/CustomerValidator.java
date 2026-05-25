package com.InformationModelingProjectManagementSystem.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.InformationModelingProjectManagementSystem.models.Customer;

@Component
public class CustomerValidator implements Validator {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(\\+7|8)\\s?\\(\\d{3}\\)\\s?\\d{3}-\\d{2}-\\d{2}$");

    @Override
    public boolean supports(Class<?> aClass) {
        return Customer.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Customer customer = (Customer) o;

        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            errors.rejectValue("name", "", "Название заказчика обязательно для заполнения");
        }

        String email = customer.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.rejectValue("email", "", "Введите корректную почту (например, name@example.com)");
            }
        }

        String phone = customer.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            long digitCount = phone.chars().filter(Character::isDigit).count();
            if (digitCount != 11) {
                errors.rejectValue("phone", "", "Номер телефона должен содержать 11 цифр");
            } else if (!PHONE_PATTERN.matcher(phone).matches()) {
                errors.rejectValue("phone", "", "Номер телефона должен быть в формате +7 (999) 123-45-67 или 8 (999) 123-45-67");
            }
        }

    }
}
