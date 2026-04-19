package com.InformationModelingProjectManagementSystem.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "Project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Название проекта не должно быть пустым")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "code", unique = true)
    private String code;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    
    @ManyToOne
    @JoinColumn(name = "responsible_person_id")
    private Person responsiblePerson;
    
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private List<Person> members = new ArrayList<>();
    
    public Project() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    
    public Person getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(Person responsiblePerson) { this.responsiblePerson = responsiblePerson; }
    
    public List<Person> getMembers() { return members; }
    public void setMembers(List<Person> members) { this.members = members; }
    
    public void addMember(Person person) {
        if (!members.contains(person)) {
            members.add(person);
        }
    }
    
    public void removeMember(Person person) {
        members.remove(person);
    }
    
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
}
