package com.InformationModelingProjectManagementSystem.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Discipline;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.repositories.DisciplineRepository;

@Service
@Transactional(readOnly = true)
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final TaskService taskService; // нужен для проверки использования

    @Autowired
    public DisciplineService(DisciplineRepository disciplineRepository, TaskService taskService) {
        this.disciplineRepository = disciplineRepository;
        this.taskService = taskService;
    }

    public List<Discipline> findAllVisible() {
        return disciplineRepository.findByVisibleTrueOrderBySortOrderAsc();
    }

    public List<Discipline> findAll() {
        return disciplineRepository.findAllByOrderBySortOrderAsc();
    }

    public Optional<Discipline> findById(int id) {
        return disciplineRepository.findById(id);
    }

    public Optional<Discipline> findByShortName(String shortName) {
        return disciplineRepository.findByShortName(shortName);
    }

    @Transactional
    public Discipline save(Discipline discipline) {
        return disciplineRepository.save(discipline);
    }

    // Проверка, используется ли раздел в каких-либо задачах
    public boolean isUsedInTasks(int disciplineId) {
        Discipline discipline = findById(disciplineId).orElse(null);
        if (discipline == null) return false;
        List<Task> tasks = taskService.findByDiscipline(discipline);
        return !tasks.isEmpty();
    }

    // Удаление раздела (только если не используется)
    @Transactional
    public boolean deleteById(int id) {
        if (!isUsedInTasks(id)) {
            disciplineRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Замена раздела во всех задачах
    @Transactional
    public void replaceDisciplineInTasks(int oldDisciplineId, int newDisciplineId) {
        Discipline oldDiscipline = findById(oldDisciplineId).orElse(null);
        Discipline newDiscipline = findById(newDisciplineId).orElse(null);
        if (oldDiscipline != null && newDiscipline != null) {
            taskService.replaceDiscipline(oldDiscipline, newDiscipline);
        }
    }

    public int getMaxSortOrder() {
        int maxOrder = 0;
        for (Discipline d : disciplineRepository.findAllByOrderBySortOrderAsc()) {
            int order = d.getSortOrder() != null ? d.getSortOrder() : 0;
            if (order > maxOrder) maxOrder = order;
        }
        return maxOrder;
    }

    public int getNextSortOrder() {
        return getMaxSortOrder() + 1;
    }
    
}