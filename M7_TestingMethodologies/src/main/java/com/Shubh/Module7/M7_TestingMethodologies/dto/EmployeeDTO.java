package com.Shubh.Module7.M7_TestingMethodologies.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class EmployeeDTO {
    Long id;
    String name;
    String email;
    Double salary;
}
