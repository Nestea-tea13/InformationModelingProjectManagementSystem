package com.InformationModelingProjectManagementSystem.controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.InformationModelingProjectManagementSystem.models.Discipline;
import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;
import com.InformationModelingProjectManagementSystem.services.DisciplineService;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.ProjectService;
import com.InformationModelingProjectManagementSystem.services.TaskService;
import com.InformationModelingProjectManagementSystem.util.TaskValidator;

@Controller
@RequestMapping("/tasks")
public class UserTaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final PeopleService peopleService;
    private final DisciplineService disciplineService;
    private final TaskValidator taskValidator;

    @Autowired
    public UserTaskController(TaskService taskService, ProjectService projectService,
                            PeopleService peopleService, DisciplineService disciplineService, 
                            TaskValidator taskValidator) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.peopleService = peopleService;
        this.disciplineService = disciplineService;
        this.taskValidator = taskValidator;
    }

    // Общий метод для инициализации карты статусов
    private Map<String, List<Task>> initTaskMap() {
        Map<String, List<Task>> map = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            map.put(status.name(), new ArrayList<>());
        }
        return map;
    }

    // Задачи, назначенные мне
    @GetMapping("/for-me")
    public String tasksForMe(@RequestParam(value = "projectId", required = false) Integer projectId,
                             Model model) {
        Person currentUser = peopleService.getCurrentPerson();

        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("userProjects", userProjects);

        List<Task> tasks;
        if (projectId != null) {
            Project project = projectService.findById(projectId).orElse(null);
            tasks = (project != null) ? taskService.findByAssigneeAndProject(currentUser, project)
                                      : taskService.findByAssignee(currentUser);
        } else {
            tasks = taskService.findByAssignee(currentUser);
        }

        Map<String, List<Task>> tasksByStatus = initTaskMap();
        for (Task task : tasks) {
            tasksByStatus.get(task.getStatus().name()).add(task);
        }

        List<Integer> statusCounts = new ArrayList<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCounts.add(tasksByStatus.get(status.name()).size());
        }
        model.addAttribute("statusCounts", statusCounts);

        model.addAttribute("tasksByStatus", tasksByStatus);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("pageTitle", "Задачи, назначенные мне");
        model.addAttribute("showExecutor", false); // не показывать исполнителя
        model.addAttribute("type", "forMe");
        return "user/tasks/tasks-kanban";
    }

    // Задачи, назначенные мной
    @GetMapping("/by-me")
    public String tasksByMe(@RequestParam(value = "projectId", required = false) Integer projectId,
                            Model model) {
        Person currentUser = peopleService.getCurrentPerson();

        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("userProjects", userProjects);

        List<Task> tasks;
        if (projectId != null) {
            Project project = projectService.findById(projectId).orElse(null);
            tasks = (project != null) ? taskService.findByAssignerAndProject(currentUser, project)
                                      : taskService.findByAssigner(currentUser);
        } else {
            tasks = taskService.findByAssigner(currentUser);
        }

        Map<String, List<Task>> tasksByStatus = initTaskMap();
        for (Task task : tasks) {
            tasksByStatus.get(task.getStatus().name()).add(task);
        }

        List<Integer> statusCounts = new ArrayList<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCounts.add(tasksByStatus.get(status.name()).size());
        }
        model.addAttribute("statusCounts", statusCounts);

        model.addAttribute("tasksByStatus", tasksByStatus);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("pageTitle", "Задачи, назначенные мной");
        model.addAttribute("showExecutor", true); // показывать исполнителя
        model.addAttribute("type", "byMe");
        return "user/tasks/tasks-kanban";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(value = "projectId", required = false) Integer projectId,
                                Model model) {
        Person currentUser = peopleService.getCurrentPerson();
        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("projects", userProjects);

        if (projectId != null) {
            Project selectedProject = projectService.findById(projectId).orElse(null);
            if (selectedProject != null && userProjects.contains(selectedProject)) {
                List<Person> availableAssignees = projectService.getAvailableAssigneesForProject(selectedProject.getId(), currentUser);
                model.addAttribute("selectedProject", selectedProject);
                model.addAttribute("members", availableAssignees);
                model.addAttribute("projectSelected", true);
            } else {
                model.addAttribute("projectSelected", false);
            }
        } else {
            model.addAttribute("projectSelected", false);
        }
        model.addAttribute("disciplines", disciplineService.findAllVisible());
        model.addAttribute("task", new Task());
        return "user/tasks/create";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("task") Task task,
                            BindingResult bindingResult,
                            @RequestParam("projectId") int projectId,
                            @RequestParam(value = "assigneeId", required = false) Integer assigneeId,
                            @RequestParam(value = "assignToSelf", required = false) boolean assignToSelf,
                            @RequestParam(value = "disciplineId", required = false) Integer disciplineId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Person currentUser = peopleService.getCurrentPerson();
        
        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект не найден");
            return "redirect:/tasks/create";
        }
        Project project = projectOpt.get();
        
        if (!projectService.hasAccess(project.getId(), currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому проекту");
            return "redirect:/tasks/create";
        }
        
        task.setProject(project);
        task.setAssigner(currentUser);
        
        // Определяем исполнителя
        Person assignee = null;
        if (assignToSelf) {
            assignee = currentUser;
        } else {
            if (assigneeId == null) {
                bindingResult.rejectValue("assignee", "", "Выберите исполнителя");
            } else {
                assignee = peopleService.findPersonById(assigneeId);
                if (assignee == null) {
                    bindingResult.rejectValue("assignee", "", "Исполнитель не найден");
                }
            }
            // Проверка прав только для случая, когда пользователь назначает не себе
            if (assignee != null && !taskService.canAssign(currentUser, assignee)) {
                bindingResult.rejectValue("assignee", "", "Вы не можете назначать задачи этому сотруднику (нарушение иерархии)");
            }
        }
        task.setAssignee(assignee);

         if (disciplineId != null && disciplineId > 0) {
            Optional<Discipline> optionalDiscipline = disciplineService.findById(disciplineId);
            if (optionalDiscipline.isPresent()) {
                task.setDiscipline(optionalDiscipline.get());
            }
        }
        
        taskValidator.validate(task, bindingResult);
        
        if (bindingResult.hasErrors()) {
            List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
            model.addAttribute("projects", userProjects);
            model.addAttribute("selectedProject", project);
            model.addAttribute("members", projectService.getAvailableAssigneesForProject(project.getId(), currentUser));
            model.addAttribute("projectSelected", true);
            return "user/tasks/create";
        }
        
        task.setStatus(TaskStatus.ASSIGNED);
        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Задача \"" + task.getTitle() + "\" создана!");
        return "redirect:/projects/" + projectId + "/tasks/" + task.getId();
    }

    @GetMapping("/list")
    public String listTasksByStatus(@RequestParam String type,
                                    @RequestParam String status,
                                    @RequestParam(required = false) Integer projectId,
                                    Model model) {
        Person currentUser = peopleService.getCurrentPerson();
        
        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("userProjects", userProjects);
        model.addAttribute("selectedProjectId", projectId);
        
        TaskStatus taskStatus;
        try {
            taskStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            taskStatus = TaskStatus.ASSIGNED;
        }
        model.addAttribute("currentStatus", taskStatus);
        model.addAttribute("statuses", TaskStatus.values());
        
        List<Task> tasks;
        if ("forMe".equals(type)) {
            if (projectId != null) {
                Project project = projectService.findById(projectId).orElse(null);
                tasks = (project != null) ? taskService.findByAssigneeAndProject(currentUser, project)
                                        : taskService.findByAssignee(currentUser);
            } else {
                tasks = taskService.findByAssignee(currentUser);
            }
            model.addAttribute("pageTitle", "Задачи, назначенные мне - ");
            model.addAttribute("showExecutor", false);
            model.addAttribute("type", "forMe");
        } else {
            if (projectId != null) {
                Project project = projectService.findById(projectId).orElse(null);
                tasks = (project != null) ? taskService.findByAssignerAndProject(currentUser, project)
                                        : taskService.findByAssigner(currentUser);
            } else {
                tasks = taskService.findByAssigner(currentUser);
            }
            model.addAttribute("pageTitle", "Задачи, назначенные мной - ");
            model.addAttribute("showExecutor", true);
            model.addAttribute("type", "byMe");
        }
        
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getStatus() == taskStatus) {
                filteredTasks.add(task);
            }
        }
        
        model.addAttribute("tasks", filteredTasks);
        return "user/tasks/list";
    }

}