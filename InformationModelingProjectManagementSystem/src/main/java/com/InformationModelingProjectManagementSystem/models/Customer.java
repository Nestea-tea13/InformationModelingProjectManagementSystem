package com.InformationModelingProjectManagementSystem.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Название заказчика не должно быть пустым")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "representative")
    private String representative;

    @Email(message = "Некорректный формат email")
    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    public Customer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

}