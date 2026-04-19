package com.InformationModelingProjectManagementSystem.controllers.admin;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/adminpage/projects")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectController {

    private final ProjectService projectService;
    private final PeopleService peopleService;
    private final ProjectValidator projectValidator;

    @Autowired
    public AdminProjectController(ProjectService projectService,
                                  PeopleService peopleService,
                                  ProjectValidator projectValidator) {
        this.projectService = projectService;
        this.peopleService = peopleService;
        this.projectValidator = projectValidator;
    }

    // Список проектов
    @GetMapping("")
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "admin/projects/list";
    }

    // Создание проекта
    @GetMapping("/create")
    public String createForm(@ModelAttribute("project") Project project, Model model) {
        model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
        model.addAttribute("statuses", ProjectStatus.values());
        return "admin/projects/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("project") Project project,
                         BindingResult bindingResult,
                         @RequestParam int responsiblePersonId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        projectValidator.validate(project, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
            model.addAttribute("statuses", ProjectStatus.values());
            return "admin/projects/create";
        }

        // Устанавливаем ответственное лицо
        Person responsiblePerson = peopleService.findPersonById(responsiblePersonId);
        if (responsiblePerson != null) {
            project.setResponsiblePerson(responsiblePerson);
        }

        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.PLANNING);
        }

        if (responsiblePerson != null) {
            project.addMember(responsiblePerson);
        }

        projectService.save(project);
        redirectAttributes.addFlashAttribute("success", "Проект \"" + project.getName() + "\" успешно создан!");

        return "redirect:/adminpage/projects";
    }

    // Редактирование проекта
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Project> optionalProject = projectService.findById(id);

        if (optionalProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/adminpage/projects";
        }

        model.addAttribute("project", optionalProject.get());
        model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
        model.addAttribute("statuses", ProjectStatus.values());

        return "admin/projects/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute("project") Project project,
                         BindingResult bindingResult,
                         @RequestParam int responsiblePersonId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        project.setId(id);
        projectValidator.validate(project, bindingResult);

        Optional<Project> existingProject = projectService.findById(id);
        if (existingProject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/adminpage/projects";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
            model.addAttribute("statuses", ProjectStatus.values());
            return "admin/projects/edit";
        }

        Project updatedProject = existingProject.get();
        updatedProject.setName(project.getName());
        updatedProject.setCode(project.getCode());
        updatedProject.setCity(project.getCity());
        updatedProject.setStatus(project.getStatus());

        Person responsiblePerson = peopleService.findPersonById(responsiblePersonId);
        if (responsiblePerson != null) {
            updatedProject.setResponsiblePerson(responsiblePerson);
            updatedProject.addMember(responsiblePerson);
        }

        projectService.save(updatedProject);
        redirectAttributes.addFlashAttribute("success", "Проект успешно обновлен!");

        return "redirect:/adminpage/projects";
    }

    // Удаление проекта
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<Project> project = projectService.findById(id);

        if (project.isPresent()) {
            String projectName = project.get().getName();
            projectService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Проект \"" + projectName + "\" успешно удален!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Проект не найден!");
        }

        return "redirect:/adminpage/projects";
    }
}