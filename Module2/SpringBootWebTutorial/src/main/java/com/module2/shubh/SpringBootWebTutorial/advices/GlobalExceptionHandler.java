package com.module2.shubh.SpringBootWebTutorial.advices;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// annotation used to define a global exception handler
// applies globally to all controllers
// ensures proper exception handling mechanism is trigerred based on the type of exception
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(NoSuchElementException exception){
        ApiError apiError = ApiError.builder().status(HttpStatus.NOT_FOUND).message("Resource Not Found").build();
        return new ResponseEntity<>(apiError,HttpStatus.NOT_FOUND);
//        return new ResponseEntity<>("Resource not found", HttpStatus.NOT_FOUND);
    }
}
