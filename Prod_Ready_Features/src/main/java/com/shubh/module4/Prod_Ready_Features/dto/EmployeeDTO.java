package com.shubh.module4.Prod_Ready_Features.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id;
    private String name;
    private String email;
    private int age;
    private String role;
    private Double salary;
    private LocalDate dateOfJoining;
    private Boolean isActive;
}