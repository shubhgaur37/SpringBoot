package com.module2.shubh.SpringBootWebTutorial.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordValidation {
    String message() default "Password should be atleast 10 characters long and contain one uppercase,lowercase and a special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
