package com.InformationModelingProjectManagementSystem.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.ProjectStatus;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Integer> {
    
    Optional<Project> findByCode(String code);
    
    List<Project> findByStatus(ProjectStatus status);
    
    List<Project> findByResponsiblePerson(Person person);
    
    List<Project> findByResponsiblePersonOrderByStartDateDesc(Person person);
    
    List<Project> findAllByOrderByNameAsc();
    
    List<Project> findByMembersContains(Person person);
    
    List<Project> findByStatusOrderByStartDateDesc(ProjectStatus status);

}