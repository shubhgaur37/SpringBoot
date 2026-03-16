package com.module2.shubh.SpringBootWebTutorial.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //validation takes place at runtime, retained in the VM running the bytecode
@Target({ElementType.FIELD}) // annotation to validate class fields
@Constraint(validatedBy = EmployeeRoleValidator.class) // specify the class with validation logic
public @interface EmployeeRoleValidation {
    String message() default "ROLE can either be EMPLOYEE or ADMIN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
