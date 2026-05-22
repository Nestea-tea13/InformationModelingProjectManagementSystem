package com.InformationModelingProjectManagementSystem.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;

@Repository
public interface TaskRepository extends CrudRepository<Task, Integer> {
    
    List<Task> findByProject(Project project);
    
    List<Task> findByAssignee(Person assignee);

    List<Task> findByAssigner(Person assigner);

    List<Task> findByAssigneeAndProject(Person assignee, Project project);
    
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
    
    List<Task> findByAssigneeAndStatus(Person assignee, TaskStatus status);
    
    List<Task> findByProjectAndAssignee(Project project, Person assignee);

    List<Task> findByAssignerAndProject(Person assigner, Project project);

}
