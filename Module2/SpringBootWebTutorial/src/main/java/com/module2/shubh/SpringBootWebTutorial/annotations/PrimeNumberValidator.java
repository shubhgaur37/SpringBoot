package com.module2.shubh.SpringBootWebTutorial.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class PrimeNumberValidator implements ConstraintValidator<PrimeNumberValidation,Integer> {
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if (integer == null) return false;
        return checkPrime(integer);
    }

    private boolean checkPrime(Integer n){
        if (n < 2)
            return false;
        for(int i = 2; i < n; i++)
            if (n%i == 0)
                return false;
        return true;
    }
}
