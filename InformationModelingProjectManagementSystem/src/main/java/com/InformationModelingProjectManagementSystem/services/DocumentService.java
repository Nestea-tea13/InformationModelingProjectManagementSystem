package com.InformationModelingProjectManagementSystem.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.InformationModelingProjectManagementSystem.models.Document;
import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;
import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;
import com.InformationModelingProjectManagementSystem.repositories.DocumentRepository;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document uploadTaskFile(Task task, MultipartFile file, Person uploader) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String storedName = timestamp + "_" + originalFilename;
        
        Path targetDir = Paths.get("uploads", "projects", String.valueOf(task.getProject().getId()))
                .toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        
        Path targetPath = targetDir.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Определяем MIME-тип по расширению
        String mimeType = determineMimeType(originalFilename);
        
        Document doc = new Document();
        doc.setProject(task.getProject());
        doc.setTask(task);
        doc.setOriginalName(originalFilename);
        doc.setStoredName(storedName);
        doc.setFilePath(targetPath.toString());
        doc.setFileSize(file.getSize());
        doc.setMimeType(mimeType);
        doc.setApproved(false);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setUploadedBy(uploader);
        
        Document saved = documentRepository.save(doc);
        documentRepository.flush();
        return saved;
    }

    private String determineMimeType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".ifc")) return "application/x-step"; // или application/octet-stream
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    public void approveTaskDocuments(Task task) {
        List<Document> docs = documentRepository.findByTask(task);
        for (Document doc : docs) {
            if (!doc.isApproved()) {
                doc.setApproved(true);
                documentRepository.save(doc);
            }
        }
    }

    public List<Document> getApprovedDocumentsForProject(Project project) {
        return documentRepository.findApprovedByProjectWithAuthor(project);
    }

    public Document findById(int id) {
        return documentRepository.findById(id).orElse(null);
    }

    public void deleteDocument(Document doc) throws IOException {
        Files.deleteIfExists(Paths.get(doc.getFilePath()));
        documentRepository.delete(doc);
    }

    public List<Document> findByTask(Task task) {
        return documentRepository.findByTask(task);
    }

    @Transactional
    public void deleteDocument(int documentId, Person currentUser, Task task) throws IOException {
        Document doc = findById(documentId);
        if (doc == null) {
            throw new IllegalArgumentException("Документ не найден");
        }
        if (!doc.getTask().equals(task)) {
            throw new SecurityException("Документ не принадлежит этой задаче");
        }
        if (task.getAssignee().getId() != currentUser.getId()) {
            throw new SecurityException("Только исполнитель может удалять файлы");
        }
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new SecurityException("Файлы можно удалять только когда задача в работе");
        }
        // Удаляем физический файл
        Files.deleteIfExists(Paths.get(doc.getFilePath()));
        // Удаляем запись из БД
        documentRepository.delete(doc);
    }
    
}