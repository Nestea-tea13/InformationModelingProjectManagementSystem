package com.InformationModelingProjectManagementSystem.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Discipline;
import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Position;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.repositories.TaskRepository;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final PositionService positionService;

    @Autowired
    public TaskService(TaskRepository taskRepository, PositionService positionService) {
        this.taskRepository = taskRepository;
        this.positionService = positionService;
    }

    public List<Task> findAll() {
        return (List<Task>) taskRepository.findAll();
    }

    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    public List<Task> findByProject(Project project) {
        return taskRepository.findByProject(project);
    }

    public List<Task> findByAssignee(Person assignee) {
        return taskRepository.findByAssignee(assignee);
    }

    public List<Task> findByAssigner(Person assigner) {
        return taskRepository.findByAssigner(assigner);
    }

    @Transactional
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteById(int id) {
        taskRepository.deleteById(id);
    }

    // Проверка, может ли пользователь назначить задачу другому пользователю (с учётом должностей)
    public boolean canAssign(Person assigner, Person assignee) {
        if (assigner.getId() == assignee.getId()) return true;
        Position assignerPos = assigner.getPosition();
        Position assigneePos = assignee.getPosition();
        if (assignerPos == null || assigneePos == null) return false;
        return positionService.canAssignTaskByPositionId(assignerPos.getId(), assigneePos.getId());
    }

    // Проверка, может ли пользователь изменить статус задачи
    public boolean canChangeStatus(Task task, Person currentUser) {
        return currentUser.getRole().equals("ROLE_ADMIN") ||
               (task.getAssignee() != null && task.getAssignee().getId() == currentUser.getId());
    }

    public List<Task> findByAssigneeAndProject(Person assignee, Project project) {
        return taskRepository.findByAssigneeAndProject(assignee, project);
    }

    public List<Task> findByAssignerAndProject(Person assigner, Project project) {
        return taskRepository.findByAssignerAndProject(assigner, project);
    }

    public List<Task> findByDiscipline(Discipline discipline) {
        return taskRepository.findByDiscipline(discipline);
    }

    @Transactional
    public void replaceDiscipline(Discipline oldDiscipline, Discipline newDiscipline) {
        List<Task> tasks = taskRepository.findByDiscipline(oldDiscipline);
        for (Task task : tasks) {
            task.setDiscipline(newDiscipline);
            taskRepository.save(task);
        }
    }
    
}