package com.InformationModelingProjectManagementSystem.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Position;
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
        return peopleRepository.findByRoleOrderBySername(role);
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
    
    public Iterable<Person> findAllWithPositions() {
        return peopleRepository.findAllByOrderBySername();
    }
    
    public List<Person> findByPosition(Position position) {
        return peopleRepository.findByPosition(position);
    }

    @Transactional
    public void updateCurrentPerson(Person updatedUser) {
        PersonDetails userDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        updatedUser.setId(userDetails.getPerson().getId());
        updatedUser.setRole(userDetails.getPerson().getRole());
        updatedUser.setPassword(userDetails.getPerson().getPassword());
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
    public void updateUserPosition(int userId, Position newPosition) {
        Person person = findPersonById(userId);
        person.setPosition(newPosition);
        peopleRepository.save(person);
    }

    @Transactional
    public Person addPerson(Person person) {
        String textEmail = "Здравствуйте, " + person.getName() + "!\nВы зарегистрированы в системе управления проектами информационного моделирования строительной компании."
                            + "\nВаша должность: " + (person.getPosition() != null ? person.getPosition().getName() : "Администратор")
                            + "\nСсылка на сайт: http://localhost:8080/\nЛогин: " + person.getEmail() 
                            + "\nПароль: " + person.generateRandomPassword() + "\nДанный пароль можно изменить в личном кабинете.";
        
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
