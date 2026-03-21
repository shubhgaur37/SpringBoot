package com.module2.shubh.SpringBootWebTutorial.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

// We need to specify inside ConstraintValidator: the validation interface and type that we want to validate upon
public class EmployeeRoleValidator implements ConstraintValidator<EmployeeRoleValidation, String> {
    @Override
    public boolean isValid(String inputRole, ConstraintValidatorContext constraintValidatorContext) {
        if (inputRole == null) return false;
        List<String> validRoles = List.of("ADMIN","USER");
        return validRoles.contains(inputRole);
    }
}
