package com.module2.shubh.SpringBootWebTutorial.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Entity
@Table(name = "departments")
public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String name;
    Boolean isActive;
    LocalDate createdAt;
}
