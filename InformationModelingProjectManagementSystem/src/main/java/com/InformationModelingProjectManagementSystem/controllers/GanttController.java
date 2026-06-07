package com.InformationModelingProjectManagementSystem.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.ProjectService;
import com.InformationModelingProjectManagementSystem.services.TaskService;

@Controller
@RequestMapping("/gantt")
public class GanttController {

    private final ProjectService projectService;
    private final PeopleService peopleService;
    private final TaskService taskService;

    @Autowired
    public GanttController(ProjectService projectService, PeopleService peopleService, TaskService taskService) {
        this.projectService = projectService;
        this.peopleService = peopleService;
        this.taskService = taskService;
    }

    @GetMapping
    public String showGantt(@RequestParam(required = false) Integer projectId, Model model) {
        Person currentUser = peopleService.getCurrentPerson();
        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("userProjects", userProjects);
        model.addAttribute("selectedProjectId", projectId);
        if (projectId != null) {
            Project project = projectService.findById(projectId).orElse(null);
            model.addAttribute("selectedProjectName", project != null ? project.getName() : "");
        }
        return "user/gantt/diagram";
    }

    @GetMapping("/data/{projectId}")
    @ResponseBody
    public List<Map<String, Object>> getGanttData(@PathVariable int projectId) {
        Project project = projectService.findById(projectId).orElse(null);
        if (project == null) return List.of();

        List<Task> tasks = taskService.findByProject(project);
        List<Map<String, Object>> ganttTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.REJECTED) continue;

            LocalDate start = task.getStartDateForGantt();
            LocalDate end = task.getDeadline();
            if (start == null || end == null) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("name", task.getTitle());
            map.put("start", start.toString());
            map.put("end", end.toString());
            map.put("color", getColorForStatus(task.getStatus()));

            // Поля для фильтрации
            map.put("status", task.getStatus().name());
            map.put("discipline", task.getDiscipline() != null ? task.getDisciplineShortName() : "Без раздела");
            map.put("assigneeName", task.getAssignee().getSername() + " " + task.getAssignee().getName());

            ganttTasks.add(map);
        }
        return ganttTasks;
    }

    private String getColorForStatus(TaskStatus status) {
        switch (status) {
            case ASSIGNED:    return "#6c757d";
            case IN_PROGRESS: return "#007bff";
            case REVIEW:      return "#ffc107";
            case COMPLETED:   return "#28a745";
            default:          return "#6c757d";
        }
    }
}