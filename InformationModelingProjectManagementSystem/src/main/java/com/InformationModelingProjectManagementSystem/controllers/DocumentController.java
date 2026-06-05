package com.InformationModelingProjectManagementSystem.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.InformationModelingProjectManagementSystem.models.Document;
import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.services.DocumentService;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.ProjectService;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ProjectService projectService;
    private final PeopleService peopleService;

    @Autowired
    public DocumentController(DocumentService documentService, ProjectService projectService, PeopleService peopleService) {
        this.documentService = documentService;
        this.projectService = projectService;
        this.peopleService = peopleService;
    }

    @GetMapping
    public String projectDocuments(@RequestParam(required = false) Integer projectId, Model model) {
        Person currentUser = peopleService.getCurrentPerson();
        List<Project> userProjects = projectService.findProjectsByParticipant(currentUser);
        model.addAttribute("userProjects", userProjects);
        model.addAttribute("selectedProjectId", projectId);

        List<Document> documents = List.of();
        if (projectId != null) {
            Project project = projectService.findById(projectId).orElse(null);
            if (project != null && projectService.hasAccess(project.getId(), currentUser)) {
                documents = documentService.getApprovedDocumentsForProject(project);
            }
        }
        model.addAttribute("documents", documents);

        Set<Integer> responsibleProjectIds = new HashSet<>();
        for (Project p : userProjects) {
            if (projectService.isResponsible(p.getId(), currentUser)) {
                responsibleProjectIds.add(p.getId());
            }
        }
        model.addAttribute("responsibleProjectIds", responsibleProjectIds);
        return "user/documents/list";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable int id) {
        Document doc = documentService.findById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        Person currentUser = peopleService.getCurrentPerson();
        if (!projectService.hasAccess(doc.getProject().getId(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Path filePath = Paths.get(doc.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            // Кодируем имя для корректной передачи кириллицы
            String encodedFileName = URLEncoder.encode(doc.getOriginalName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Просмотр файла (inline) для PDF и изображений
    @GetMapping("/view-file/{id}")
    public ResponseEntity<Resource> viewFile(@PathVariable int id) {
        Document doc = documentService.findById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        Person currentUser = peopleService.getCurrentPerson();
        if (!projectService.hasAccess(doc.getProject().getId(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Path filePath = Paths.get(doc.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            String encodedFileName = URLEncoder.encode(doc.getOriginalName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            ContentDisposition contentDisposition = ContentDisposition.inline()
                    .filename(encodedFileName)
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view/{id}")
    public String viewDocument(@PathVariable int id, Model model) {
        Document doc = documentService.findById(id);
        if (doc == null) return "redirect:/documents";
        Person currentUser = peopleService.getCurrentPerson();
        if (!projectService.hasAccess(doc.getProject().getId(), currentUser)) {
            return "redirect:/documents?error=Доступ запрещён";
        }
        model.addAttribute("doc", doc);
        return "user/documents/view";
    }

    // Проверка, может ли пользователь удалить документ (только руководитель проекта) на странице перечня документов
    private boolean canDeleteDocument(Document doc, Person currentUser) {
        return projectService.isResponsible(doc.getProject().getId(), currentUser);
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable int id, RedirectAttributes redirectAttributes) throws IOException {
        Document doc = documentService.findById(id);
        if (doc == null) {
            redirectAttributes.addFlashAttribute("error", "Документ не найден");
            return "redirect:/documents";
        }
        Person currentUser = peopleService.getCurrentPerson();
        if (!canDeleteDocument(doc, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на удаление этого документа");
            return "redirect:/documents?projectId=" + doc.getProject().getId();
        }
        documentService.deleteDocument(doc);
        redirectAttributes.addFlashAttribute("success", "Документ удалён");
        return "redirect:/documents?projectId=" + doc.getProject().getId();
    }
    
}
