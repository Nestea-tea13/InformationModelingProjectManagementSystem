package com.InformationModelingProjectManagementSystem.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Position;

@Repository
public interface PositionRepository extends CrudRepository<Position, Integer> {
    
    Optional<Position> findByName(String name);
    
    Iterable<Position> findAllByOrderByNameAsc();

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Position p " +
        "JOIN p.canAssignTo sub WHERE p.id = :assignerId AND sub.id = :subordinateId")
    boolean canAssignByPositionId(@Param("assignerId") int assignerId, 
                                @Param("subordinateId") int subordinateId);
    
}
