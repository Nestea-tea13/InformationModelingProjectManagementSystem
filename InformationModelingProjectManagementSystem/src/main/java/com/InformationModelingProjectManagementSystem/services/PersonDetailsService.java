package com.InformationModelingProjectManagementSystem.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.repositories.PeopleRepository;
import com.InformationModelingProjectManagementSystem.security.PersonDetails;

@Service
public class PersonDetailsService implements UserDetailsService {
    
    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonDetailsService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Person> person = peopleRepository.findByEmail(s);

        if (person.isEmpty())
            throw new UsernameNotFoundException("Пользователь не найден!");

        return new PersonDetails(person.get());
    }
    
}
