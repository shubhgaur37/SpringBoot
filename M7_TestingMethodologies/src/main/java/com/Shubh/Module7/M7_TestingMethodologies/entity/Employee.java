package com.Shubh.Module7.M7_TestingMethodologies.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {
    @Id
    Long id;
    String name;
    @Column(unique = true)
    String email;
    Double salary;
}
