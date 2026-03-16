package com.module2.shubh.SpringBootWebTutorial.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

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
    // Check's if the string is not null and string length after trimming blank spaces from the ends is > 0
    @NotBlank(message = "Name of the employee cannot be blank")
//    enforces size limits on name
    @Size(min= 3, max = 10, message = "Should be of size in the interval [3,10]")
    private String name;
    @NotNull
    @Email(message = "Email should be in valid format") // does not enforce not null constraint
    private String email;
    @NotNull(message = "Age of employees cannot be null")
    @Min(value = 18,message = "Age cannot be less than 18")
    @Max(value = 80,message = "Age cannot be more than 80")
    private int age;

    @Pattern(regexp = "^(ADMIN|USER)$", message = "ROLE can be EMPLOYEE | ADMIN")
    @NotBlank(message = "Role of Employee cannot be blank") // enforces not null constraint as well
    private String role;
    @NotNull(message="Salary cannot be null") @Positive(message = "Salary should be positive")
    // digits does not consider all zeroes after decimal i.e. xxxx.00000000 is valid and won't fail the request
    @Digits(integer = 6, fraction = 2, message = "Salary before decimal cannot have more than 6 digits and after decimal limit = 2")
    @DecimalMin(value = "100.50")@DecimalMax(value = "100000.99") // default message is used here
    private Double salary;
    @PastOrPresent(message = "Date of Joining cannot be in the future")
    private LocalDate dateOfJoining;
//    Jackson's default behavior is to omit the "is" prefix for primitive boolean fields during serialization
//    this specifies how this field would be serialized/deserialized in/from the response
//    Serialize: JAVA to JSON
//    Deserialize: JSON TO JAVA
//    @JsonProperty("isActive")
    @AssertTrue(message = "Employee should be active")
    private Boolean isActive;
}