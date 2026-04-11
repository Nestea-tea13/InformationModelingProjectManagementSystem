package com.InformationModelingProjectManagementSystem.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.repositories.PeopleRepository;
import com.InformationModelingProjectManagementSystem.security.PersonDetails;

@Service
@Transactional(readOnly = true)
public class PeopleService {
    
    private final PeopleRepository peopleRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public Iterable<Person> findByRole(String role) {
        Iterable<Person> employees = peopleRepository.findByRoleOrderBySername(role);
        return employees;
    }

    public Optional<Person> findByEmail(String email) {
        return peopleRepository.findByEmail(email);
    }

    public Person findPersonById(int id) {
        return peopleRepository.findById(id).get();
    }

    public Boolean existsPersonById(int id) {
        return peopleRepository.existsById(id);
    }

    public Person getCurrentPerson() {
        PersonDetails userDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getPerson();
    }

    @Transactional
    public void updateCurrentPerson(Person updatedUser) {
        PersonDetails userDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        updatedUser.setId(userDetails.getPerson().getId());
        userDetails.setPerson(updatedUser);
        peopleRepository.save(updatedUser);
    }

    @Transactional
    public void updatePassword(String password) {
        Person currentPerson = getCurrentPerson();
        currentPerson.setPassword(password);
        peopleRepository.save(currentPerson);
    }

    @Transactional
    public Person addPerson(Person person) {
        String textEmail = "Здравствуйте, " + person.getName() + "!\nВы зарегистрированы в системе управления проектами информационного моделирования строительной компании."
                            + "\nВаша должность: " + person.getPosition(); 
        
        String randomPassword = person.generateRandomPassword();

        textEmail += "\nСсылка на сайт: http://localhost:8080/\nЛогин: " + person.getEmail() 
                + "\nПароль: " + randomPassword + "\nДанный пароль можно изменить в личном кабинете.";
        
        peopleRepository.save(person);

        mailSender.send(person.getEmail(), "Регистрация в системе управления проектами", textEmail);
        return person;
    }

    @Transactional
    public void update(int id, Person updatedUser) {
        updatedUser.setId(id);
        peopleRepository.save(updatedUser);
    }

    @Transactional
    public void removePerson(int id) {
        Person person = peopleRepository.findById(id).orElseThrow();
        peopleRepository.delete(person);
    }

}
