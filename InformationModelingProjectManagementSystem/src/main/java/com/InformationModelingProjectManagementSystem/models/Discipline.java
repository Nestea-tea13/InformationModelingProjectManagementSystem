package com.InformationModelingProjectManagementSystem.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "discipline")
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Сокращение раздела обязательно")
    @Pattern(regexp = "^[А-ЯA-Z\\s]+$", message = "Сокращение должно быть заглавными буквами")
    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    @NotBlank(message = "Полное название обязательно")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Discipline() {}

    public Discipline(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
}