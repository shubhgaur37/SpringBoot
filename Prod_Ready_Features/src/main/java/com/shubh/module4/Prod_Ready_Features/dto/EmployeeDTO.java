package com.shubh.module4.Prod_Ready_Features.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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