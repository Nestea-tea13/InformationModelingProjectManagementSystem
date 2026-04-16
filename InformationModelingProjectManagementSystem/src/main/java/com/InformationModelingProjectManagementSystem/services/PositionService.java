package com.InformationModelingProjectManagementSystem.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Position;
import com.InformationModelingProjectManagementSystem.repositories.PositionRepository;

@Service
@Transactional(readOnly = true)
public class PositionService {
    
    private final PositionRepository positionRepository;
    
    @Autowired
    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }
    
    public Iterable<Position> findAll() {
        return positionRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<Position> findById(int id) {
        return positionRepository.findById(id);
    }
    
    public Optional<Position> findByName(String name) {
        return positionRepository.findByName(name);
    }
    
    @Transactional
    public Position save(Position position) {
        return positionRepository.save(position);
    }
    
    @Transactional
    public void deleteById(int id) {
        positionRepository.deleteById(id);
    }
    
    // Проверка, может ли должность-начальник назначать задачи должности-подчиненному
    public boolean canAssignTask(Position assignerPosition, Position assigneePosition) {
        if (assignerPosition == null || assigneePosition == null) {
            return false;
        }
        return assignerPosition.canAssignTo(assigneePosition);
    }
    
    // Проверка для пользователей
    public boolean canAssignTask(Person assigner, Person assignee) {
        // Администраторы не участвуют в иерархии задач
        if (assigner.getRole().equals("ROLE_ADMIN") || assignee.getRole().equals("ROLE_ADMIN")) {
            return false;
        }
        return canAssignTask(assigner.getPosition(), assignee.getPosition());
    }
    
}
