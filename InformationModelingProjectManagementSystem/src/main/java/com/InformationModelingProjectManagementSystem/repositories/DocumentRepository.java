package com.InformationModelingProjectManagementSystem.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Document;
import com.InformationModelingProjectManagementSystem.models.Project;
import com.InformationModelingProjectManagementSystem.models.Task;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByTask(Task task);

    List<Document> findByProjectAndApprovedTrueOrderByUploadedAtDesc(Project project);

    @Query("SELECT d FROM Document d JOIN FETCH d.uploadedBy WHERE d.project = :project AND d.approved = true ORDER BY d.uploadedAt DESC")
    List<Document> findApprovedByProjectWithAuthor(@Param("project") Project project);
    
}