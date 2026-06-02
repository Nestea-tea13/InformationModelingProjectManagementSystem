package com.InformationModelingProjectManagementSystem.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Discipline;

@Repository
public interface DisciplineRepository extends CrudRepository<Discipline, Integer> {

    List<Discipline> findByVisibleTrueOrderBySortOrderAsc();

    List<Discipline> findAllByOrderBySortOrderAsc();

    Optional<Discipline> findByShortName(String shortName);
    
}
