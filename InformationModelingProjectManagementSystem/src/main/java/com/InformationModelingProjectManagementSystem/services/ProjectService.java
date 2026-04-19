package com.InformationModelingProjectManagementSystem.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.ProjectStatus;
import com.InformationModelingProjectManagementSystem.repositories.ProjectRepository;

@Service
@Transactional(readOnly = true)
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    
    public List<Project> findAll() {
        return projectRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<Project> findById(int id) {
        return projectRepository.findById(id);
    }
    
    public Optional<Project> findByCode(String code) {
        return projectRepository.findByCode(code);
    }
    
    public List<Project> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }
    
    public List<Project> findByResponsiblePerson(Person person) {
        return projectRepository.findByResponsiblePerson(person);
    }
    
    public List<Project> findByMember(Person person) {
        return projectRepository.findByMembersContains(person);
    }
    
    public boolean existsByCode(String code) {
        return projectRepository.findByCode(code).isPresent();
    }
    
    // Проверка, является ли пользователь ответственным за проект
    public boolean isResponsible(int projectId, Person person) {
        if (person == null) return false;
        Optional<Project> project = findById(projectId);
        return project.isPresent() && 
               project.get().getResponsiblePerson() != null && 
               project.get().getResponsiblePerson().getId() == person.getId();
    }
    
    // Проверка, является ли пользователь участником проекта
    public boolean isMember(int projectId, Person person) {
        if (person == null) return false;
        Optional<Project> project = findById(projectId);
        return project.isPresent() && 
               project.get().getMembers().stream()
                   .anyMatch(member -> member.getId() == person.getId());
    }
    
    // Доступ к проекту (ответственный или участник)
    public boolean hasAccess(int projectId, Person person) {
        return isResponsible(projectId, person) || isMember(projectId, person);
    }
    
    @Transactional
    public Project save(Project project) {
        return projectRepository.save(project);
    }
    
    @Transactional
    public void deleteById(int id) {
        projectRepository.deleteById(id);
    }
    
    @Transactional
    public void addMember(int projectId, Person person) {
        Optional<Project> optionalProject = findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            project.addMember(person);
            projectRepository.save(project);
        }
    }
    
    @Transactional
    public void removeMember(int projectId, Person person) {
        Optional<Project> optionalProject = findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            project.removeMember(person);
            projectRepository.save(project);
        }
    }
    
    @Transactional
    public void changeStatus(int projectId, ProjectStatus newStatus) {
        Optional<Project> optionalProject = findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            project.setStatus(newStatus);
            projectRepository.save(project);
        }
    }
    
}
