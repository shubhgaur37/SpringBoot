package com.module2.shubh.SpringBootWebTutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// POJO: Plain Old Java Object
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
//    Using Lombok to get rid of boilerplate code
//    needed a non-primitive type here as DTO to Entity Conversion is not working
//    because primary key is set to autogenerate in the entity and its being passed as 0
    private Long id;
    private String name;
    private String email;
    private int age;
    private LocalDate dateOfJoining;
    private Boolean isActive;
}
