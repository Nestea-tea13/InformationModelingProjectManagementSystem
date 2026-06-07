package com.InformationModelingProjectManagementSystem.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;
import com.InformationModelingProjectManagementSystem.services.DocumentService;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.ProjectService;
import com.InformationModelingProjectManagementSystem.services.TaskService;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final PeopleService peopleService;
    private final DocumentService documentService;

    @Autowired
    public TaskController(TaskService taskService, ProjectService projectService, 
                          PeopleService peopleService, DocumentService documentService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.peopleService = peopleService;
        this.documentService = documentService;
    }

    // Просмотр задачи
    @GetMapping("/{taskId}")
    public String viewTask(@PathVariable int projectId, @PathVariable int taskId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Project> projectOpt = projectService.findById(projectId);
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (projectOpt.isEmpty() || taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Проект или задача не найдены");
            return "redirect:/projects";
        }
        Task task = taskOpt.get();
        if (task.getProject().getId() != projectId) {
            redirectAttributes.addFlashAttribute("error", "Задача не принадлежит указанному проекту");
            return "redirect:/projects";
        }
        Person currentUser = peopleService.getCurrentPerson();
        boolean isAssignee = currentUser.getId() == task.getAssignee().getId();
        boolean isAssigner = currentUser.getId() == task.getAssigner().getId();
        model.addAttribute("project", projectOpt.get());
        model.addAttribute("task", task);
        model.addAttribute("isAssignee", isAssignee);
        model.addAttribute("isAssigner", isAssigner);
        model.addAttribute("documents", documentService.findByTask(task));
        return "user/tasks/view";
    }

    // Принять в работу
    @PostMapping("/{taskId}/start")
    public String startTask(@PathVariable int projectId, @PathVariable int taskId, RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks";
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();
        if (currentUser.getId() != task.getAssignee().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только исполнитель может принять задачу в работу");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        if (task.getPlannedStartDate() == null) {
            task.setPlannedStartDate(LocalDate.now());
        }

        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Задача принята в работу");
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Отправить на проверку
    @PostMapping("/{taskId}/review")
    public String reviewTask(@PathVariable int projectId, @PathVariable int taskId, RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks";
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();
        if (currentUser.getId() != task.getAssignee().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только исполнитель может отправить задачу на проверку");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        task.setStatus(TaskStatus.REVIEW);
        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Задача отправлена на проверку");
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Отклонить (исполнитель)
    @PostMapping("/{taskId}/reject")
    public String rejectTask(@PathVariable int projectId, @PathVariable int taskId,
                             @RequestParam(required = false) String comment,
                             RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks";
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();
        if (currentUser.getId() != task.getAssignee().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только исполнитель может отклонить задачу");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        task.setStatus(TaskStatus.REJECTED);
        if (comment != null && !comment.trim().isEmpty()) {
            task.setStatusComment(comment);
        }
        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Задача отклонена" + (comment != null ? " с комментарием: " + comment : ""));
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Принять – автор
    @PostMapping("/{taskId}/accept")
    public String acceptTask(@PathVariable int projectId, @PathVariable int taskId, RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks";
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();
        if (currentUser.getId() != task.getAssigner().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только автор задачи может принять её выполнение");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        task.setStatus(TaskStatus.COMPLETED);
        taskService.save(task);
        documentService.approveTaskDocuments(task);
        redirectAttributes.addFlashAttribute("success", "Задача выполнена");
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Вернуть в работу (автор)
    @PostMapping("/{taskId}/return")
    public String returnTask(@PathVariable int projectId, @PathVariable int taskId,
                             @RequestParam(required = false) String comment,
                             RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks";
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();
        if (currentUser.getId() != task.getAssigner().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только автор задачи может вернуть её в работу");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        if (comment != null && !comment.trim().isEmpty()) {
            task.setStatusComment(comment);
        }
        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Задача возвращена в работу" + (comment != null ? " с комментарием: " + comment : ""));
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Загрузка файлов к задаче (только исполнитель, статус IN_PROGRESS)
    @PostMapping("/{taskId}/upload")
    public String uploadFiles(@PathVariable int projectId,
                              @PathVariable int taskId,
                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                              RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();

        if (currentUser.getId() != task.getAssignee().getId()) {
            redirectAttributes.addFlashAttribute("error", "Только исполнитель может загружать файлы");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            redirectAttributes.addFlashAttribute("error", "Файлы можно загружать только когда задача в работе");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        documentService.uploadTaskFile(task, file, currentUser);
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Ошибка загрузки файла: " + file.getOriginalFilename());
                        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
                    }
                }
            }
            redirectAttributes.addFlashAttribute("success", "Файлы успешно загружены");
        } else {
            redirectAttributes.addFlashAttribute("info", "Файлы не выбраны");
        }
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    @PostMapping("/{taskId}/delete-file/{documentId}")
    public String deleteFile(@PathVariable int projectId,
                            @PathVariable int taskId,
                            @PathVariable int documentId,
                            RedirectAttributes redirectAttributes) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/projects/" + projectId + "/tasks/" + taskId;
        }
        Task task = taskOpt.get();
        Person currentUser = peopleService.getCurrentPerson();

        try {
            documentService.deleteDocument(documentId, currentUser, task);
            redirectAttributes.addFlashAttribute("success", "Файл удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    @GetMapping("/{taskId}/data")
    @ResponseBody
    public Map<String, Object> getTaskData(@PathVariable int projectId, @PathVariable int taskId) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) return Map.of();
        Task task = taskOpt.get();
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", task.getId());
        data.put("title", task.getTitle());
        data.put("discipline", task.getDisciplineShortName());
        data.put("statusName", task.getStatusDisplayName());
        data.put("startDate", task.getStartDateForGantt().toString());
        data.put("endDate", task.getDeadline().toString());
        data.put("assignerName", task.getAssigner().getSername() + " " + task.getAssigner().getName());
        data.put("assigneeName", task.getAssignee().getSername() + " " + task.getAssignee().getName());
        
        return data;
    }
    
}