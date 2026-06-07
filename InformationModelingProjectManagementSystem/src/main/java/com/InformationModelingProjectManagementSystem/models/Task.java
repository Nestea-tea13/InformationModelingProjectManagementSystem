package com.InformationModelingProjectManagementSystem.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.InformationModelingProjectManagementSystem.models.enums.TaskStatus;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assigner_id", nullable = false)
    private Person assigner;

    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = false)
    private Person assignee;

    @NotEmpty(message = "Название задачи не может быть пустым")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Срок выполнения обязателен")
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "status_comment", length = 500)
    private String statusComment;

    @ManyToOne
    @JoinColumn(name = "discipline_id")
    private Discipline discipline;

    public Task() {
        this.createdAt = LocalDate.now();
        this.status = TaskStatus.ASSIGNED;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Person getAssigner() { return assigner; }
    public void setAssigner(Person assigner) { this.assigner = assigner; }

    public Person getAssignee() { return assignee; }
    public void setAssignee(Person assignee) { this.assignee = assignee; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; }
    
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getStatusComment() { return statusComment; }
    public void setStatusComment(String statusComment) { this.statusComment = statusComment; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    public String getFormattedDeadline() {
        return deadline != null ? deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "—";
    }

    public boolean isOverdue() {
        return deadline != null && deadline.isBefore(LocalDate.now());
    }

    public String getDisciplineFullName() {
        return discipline != null ? discipline.getFullName() : "—";
    }

    public String getDisciplineShortName() {
        return discipline != null ? discipline.getShortName() : "—";
    }

    // Получение даты начала для диаграммы
    public LocalDate getStartDateForGantt() {
        return plannedStartDate != null ? plannedStartDate : createdAt;
    }
    
}