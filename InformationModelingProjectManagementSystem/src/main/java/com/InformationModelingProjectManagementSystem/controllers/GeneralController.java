package com.InformationModelingProjectManagementSystem.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.InformationModelingProjectManagementSystem.data.DateBorders;
import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.subsidiaryClasses.ChangePassword;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.util.ChangePasswordValidator;
import com.InformationModelingProjectManagementSystem.util.PersonValidator;

@Controller
public class GeneralController {
    
    private final PeopleService peopleService;
    private final PersonValidator personValidator;
    private final ChangePasswordValidator changePasswordValidator;

    @Autowired
    public GeneralController(PeopleService peopleService, PersonValidator personValidator, ChangePasswordValidator changePasswordValidator) { 
        this.peopleService = peopleService;
        this.personValidator = personValidator;
        this.changePasswordValidator = changePasswordValidator;
    }
    
    @GetMapping({"/profile", "/adminpage/profile"})
    public String openProfile(Model model) {
        model.addAttribute("person", peopleService.getCurrentPerson());
        model.addAttribute("flagRoleAdmin", peopleService.getCurrentPerson().getRole().equals("ROLE_ADMIN"));
        return "general/profile-page";
    }

    @GetMapping({"/profile/edit", "/adminpage/profile/edit"})
    public String profileEdit(Model model) {
        Person currentPerson = peopleService.getCurrentPerson();
        model.addAttribute("person", currentPerson);
        model.addAttribute("flagRoleAdmin", currentPerson.getRole().equals("ROLE_ADMIN"));
        model.addAttribute("birthdayDateBorders", DateBorders.getBirthdayBorders());
        return "general/profile-edit";
    }

    @PostMapping({"/profile", "/adminpage/profile"})
    public String profileUpdate(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult, Model model) {
        boolean flagRoleAdmin = peopleService.getCurrentPerson().getRole().equals("ROLE_ADMIN");

        personValidator.validate(person, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("flagRoleAdmin", flagRoleAdmin);
            model.addAttribute("birthdayDateBorders", DateBorders.getBirthdayBorders());
            return "general/profile-edit";
        }

        peopleService.updateCurrentPerson(person);
        if(flagRoleAdmin) return "redirect:/adminpage/profile";
        else return "redirect:/profile";
    }

    @GetMapping({"/profile/changepassword", "/adminpage/profile/changepassword"})
    public String changePassword(@ModelAttribute("passwords") ChangePassword passwords, Model model) {
        model.addAttribute("flagRoleAdmin", peopleService.getCurrentPerson().getRole().equals("ROLE_ADMIN"));
        return "general/change-password";
    }

    @PostMapping({"/profile/changepassword", "/adminpage/profile/changepassword"})
    public String saveEditPassword(@ModelAttribute("passwords") @Valid ChangePassword passwords, BindingResult bindingResult, Model model) {     

        changePasswordValidator.validate(passwords, peopleService.getCurrentPerson(), bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("flagRoleAdmin", peopleService.getCurrentPerson().getRole().equals("ROLE_ADMIN"));
            return "general/change-password";
        }

        peopleService.updatePassword(passwords.getNewPasswordEncoder());

        if(peopleService.getCurrentPerson().getRole().equals("ROLE_ADMIN")) return "redirect:/adminpage/profile";
        else return "redirect:/profile";
    }
    
}
