package com.module2.shubh.SpringBootWebTutorial.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Fundamental part of Object-Relationship Model (ORM) paradigm
@Entity // This notation tells hibernate that we need a table of this class
// @Table : used for defining the name of table, table would be created even if this annotation is
// not specified but with class name, @Table gives us flexibility to add properties like indexes, constraints
@Table(name = "employees")
// Queries returning an empty response because of private fields and no getters
// due to which JDBC is struggling to return proper responses
// we will use lombok annotations to make our life easy and define
// getters setters constructors for us using annotations
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEntity {
    @Id // used to specify primary key for the table
    // used to enforce strategy to ensure uniqueness eg. auto-increment, sequence,
    // here it chooses generation strategy automatically
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String email;
    private int age;
    private LocalDate dateOfJoining;
    private Boolean isActive;
    private String role;
    private Double salary;
}

// this is not code duplication as it might look comparing EmployeeDTO
// In case we want to store sensitive information here and avoid exposing it on the dto side.
// DTO is used to specify request response validation logic, while this class represents the constraints,
// schema, properties that these fields must adhere to in the DB

