package com.module2.shubh.SpringBootWebTutorial.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValidation, String> {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{10,}$";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null) return false;

        return s.matches(PASSWORD_REGEX);
    }
}