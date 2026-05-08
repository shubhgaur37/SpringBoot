package com.Module3.Practice.CollegeManagement.dto.student;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Simple input for student registration.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentRequestDTO {
    @NotBlank(message = "Name is required")
    String name;
}