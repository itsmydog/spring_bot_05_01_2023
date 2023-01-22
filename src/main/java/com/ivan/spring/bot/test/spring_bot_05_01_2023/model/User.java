package com.ivan.spring.bot.test.spring_bot_05_01_2023.model;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

@Component

@Entity
@Table(name = "employees")
public class User {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "surname")
    private String surName;




    public User() {
    }

    public User(String name, String country) {
        this.name = name;
        this.surName = country;
    }

    public long getId() {
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

    public String getSurName() {
        return surName;
    }

    public void setSurName(String country) {
        this.surName = country;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "department_id")
    private Departments departments;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + surName + '\'' +
                '}';
    }

    public Departments getDepartments() {
        return departments;
    }

    public void setDepartments(Departments departments) {
        this.departments = departments;
    }
}
