package com.module2.shubh.SpringBootWebTutorial.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentDTO {
    Long id;
    @NotBlank(message = "Department Name cannot be blank")
    String name;
    @AssertTrue(message = "Department should be active")
    Boolean isActive;
    @PastOrPresent(message = "Date of Department creation cannot be in the future")
    LocalDate createdAt;
}

