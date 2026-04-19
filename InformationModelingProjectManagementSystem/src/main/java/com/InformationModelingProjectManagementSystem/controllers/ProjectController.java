package com.InformationModelingProjectManagementSystem.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.ProjectStatus;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.ProjectService;
import com.InformationModelingProjectManagementSystem.util.ProjectValidator;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    
    private final ProjectService projectService;
    private final PeopleService peopleService;
    private final ProjectValidator projectValidator;
    
    @Autowired
    public ProjectController(ProjectService projectService, 
                             PeopleService peopleService,
                             ProjectValidator projectValidator) {
        this.projectService = projectService;
        this.peopleService = peopleService;
        this.projectValidator = projectValidator;
    }
    
    // Список проектов
    @GetMapping("")
    public String listProjects(Model model, RedirectAttributes redirectAttributes) {
        Person currentUser = peopleService.getCurrentPerson();
        
        // Для администратора редирект на админскую страницу
        if (currentUser.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/adminpage/projects";
        }
        
        // Проекты, где пользователь ответственный
        List<Project> responsibleProjects = projectService.findByResponsiblePerson(currentUser);
        
        // Все проекты, где пользователь участник
        List<Project> allMemberProjects = projectService.findByMember(currentUser);
        
        // Фильтруем: убираем проекты, где пользователь ответственный
        List<Project> memberProjects = new ArrayList<>();
        for (Project project : allMemberProjects) {
            if (project.getResponsiblePerson() == null || 
                project.getResponsiblePerson().getId() != currentUser.getId()) {
                memberProjects.add(project);
            }
        }
        
        model.addAttribute("responsibleProjects", responsibleProjects);
        model.addAttribute("memberProjects", memberProjects);
        model.addAttribute("currentUserId", currentUser.getId());
        
        return "user/projects/list";
    }
    
    // Просмотр проекта
    @GetMapping("/{id}")
    public String viewProject(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Project> optionalProject = projectService.findById(id);
        Person currentUser = peopleService.getCurrentPerson();
        
        if (optionalProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/projects";
        }
        
        Project project = optionalProject.get();
        
        // Проверка доступа: администратор или участник проекта
        if (!currentUser.getRole().equals("ROLE_ADMIN") && 
            !projectService.hasAccess(id, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому проекту");
            return "redirect:/projects";
        }
        
        model.addAttribute("project", project);
        model.addAttribute("isResponsible", projectService.isResponsible(id, currentUser));
        model.addAttribute("isAdmin", currentUser.getRole().equals("ROLE_ADMIN"));
        
        return "user/projects/view";
    }
    
    // Редактирование основной информации
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Project> optionalProject = projectService.findById(id);
        Person currentUser = peopleService.getCurrentPerson();
        
        if (optionalProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/projects";
        }
        
        Project project = optionalProject.get();
        
        // Проверка прав: только ответственное лицо
        if (!projectService.isResponsible(id, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование этого проекта");
            return "redirect:/projects/" + id;
        }
        
        model.addAttribute("project", project);
        model.addAttribute("statuses", ProjectStatus.values());
        
        return "user/projects/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute("project") Project project,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        
        project.setId(id);
        projectValidator.validate(project, bindingResult);
        
        Optional<Project> existingProject = projectService.findById(id);
        Person currentUser = peopleService.getCurrentPerson();
        
        if (existingProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/projects";
        }
        
        // Проверка прав
        if (!projectService.isResponsible(id, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование этого проекта");
            return "redirect:/projects/" + id;
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ProjectStatus.values());
            return "user/projects/edit";
        }
        
        Project updatedProject = existingProject.get();
        updatedProject.setName(project.getName());
        updatedProject.setCode(project.getCode());
        updatedProject.setDescription(project.getDescription());
        updatedProject.setCity(project.getCity());
        updatedProject.setAddress(project.getAddress());
        updatedProject.setStartDate(project.getStartDate());
        updatedProject.setEndDate(project.getEndDate());
        updatedProject.setStatus(project.getStatus());
        
        projectService.save(updatedProject);
        redirectAttributes.addFlashAttribute("success", "Основная информация проекта обновлена!");
        
        return "redirect:/projects/" + id;
    }
    
    // Редактирование списка участников
    @GetMapping("/{id}/edit-members")
    public String editMembersForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Project> optionalProject = projectService.findById(id);
        Person currentUser = peopleService.getCurrentPerson();
        
        if (optionalProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/projects";
        }
        
        Project project = optionalProject.get();
        
        // Проверка прав: только ответственное лицо
        if (!projectService.isResponsible(id, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование участников");
            return "redirect:/projects/" + id;
        }
        
        model.addAttribute("project", project);
        model.addAttribute("allUsers", peopleService.findByRole("ROLE_USER"));
        
        return "user/projects/edit-members";
    }
    
    @PostMapping("/{id}/edit-members")
    public String updateMembers(@PathVariable int id,
                                @RequestParam(value = "memberIds", required = false) List<Integer> memberIds,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Project> existingProject = projectService.findById(id);
        Person currentUser = peopleService.getCurrentPerson();
        
        if (existingProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/projects";
        }
        
        // Проверка прав
        if (!projectService.isResponsible(id, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование участников");
            return "redirect:/projects/" + id;
        }
        
        Project updatedProject = existingProject.get();
        
        // Обновляем список участников
        updatedProject.getMembers().clear();
        
        if (memberIds != null) {
            for (Integer memberId : memberIds) {
                Person member = peopleService.findPersonById(memberId);
                if (member != null) {
                    updatedProject.addMember(member);
                }
            }
        }
        
        // Убеждаемся, что ответственный есть в списке участников
        if (updatedProject.getResponsiblePerson() != null) {
            updatedProject.addMember(updatedProject.getResponsiblePerson());
        }
        
        projectService.save(updatedProject);
        redirectAttributes.addFlashAttribute("success", "Состав участников проекта обновлен!");
        
        return "redirect:/projects/" + id;
    }
}