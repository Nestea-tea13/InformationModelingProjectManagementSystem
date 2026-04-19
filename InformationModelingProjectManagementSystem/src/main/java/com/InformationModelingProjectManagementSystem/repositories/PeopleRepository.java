package com.InformationModelingProjectManagementSystem.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Position;

@Repository
public interface PeopleRepository extends CrudRepository<Person, Integer> {

    Optional<Person> findByEmail(String email);

    List<Person> findByRoleOrderBySername(String role);
    
    List<Person> findByPosition(Position position);
    
    List<Person> findAllByOrderBySername();

}