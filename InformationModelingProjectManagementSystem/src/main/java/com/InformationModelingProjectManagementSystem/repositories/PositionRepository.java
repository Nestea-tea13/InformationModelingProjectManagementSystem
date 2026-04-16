package com.InformationModelingProjectManagementSystem.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Position;

@Repository
public interface PositionRepository extends CrudRepository<Position, Integer> {
    
    Optional<Position> findByName(String name);
    
    Iterable<Position> findAllByOrderByNameAsc();
    
}
