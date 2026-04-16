package com.InformationModelingProjectManagementSystem.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "position")
public class Position {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotEmpty(message = "Должность не должна быть пустой")
    @Column(unique = true, nullable = false)
    private String name;
    
    // КОМУ ЭТА ДОЛЖНОСТЬ МОЖЕТ НАЗНАЧАТЬ ЗАДАЧИ (подчиненные)
    @ManyToMany
    @JoinTable(
        name = "position_subordinates",
        joinColumns = @JoinColumn(name = "position_id"),
        inverseJoinColumns = @JoinColumn(name = "subordinate_position_id")
    )
    private List<Position> canAssignTo = new ArrayList<>();
    
    // КТО МОЖЕТ НАЗНАЧАТЬ ЗАДАЧИ ЭТОЙ ДОЛЖНОСТИ (начальники) - обратная связь
    @ManyToMany(mappedBy = "canAssignTo")
    private List<Position> canBeAssignedBy = new ArrayList<>();
    
    public Position() {}
    
    public Position(String name) {
        this.name = name;
    }
    
    public int getId() { 
        return id; 
    }

    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }
    
    public List<Position> getCanAssignTo() { 
        return canAssignTo; 
    }

    public void setCanAssignTo(List<Position> canAssignTo) { 
        this.canAssignTo = canAssignTo; 
    }
    
    public List<Position> getCanBeAssignedBy() { 
        return canBeAssignedBy; 
    }
    
    // Добавление подчиненного
    public void addSubordinate(Position subordinate) {
        if (!this.canAssignTo.contains(subordinate)) {
            this.canAssignTo.add(subordinate);
            subordinate.canBeAssignedBy.add(this);
        }
    }
    
    // Удаление подчиненного
    public void removeSubordinate(Position subordinate) {
        this.canAssignTo.remove(subordinate);
        subordinate.canBeAssignedBy.remove(this);
    }

    // Проверка, может ли эта должность назначать задачи другой
    public boolean canAssignTo(Position other) {
        return this.canAssignTo.contains(other);
    }

}
