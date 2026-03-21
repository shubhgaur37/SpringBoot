package com.module2.shubh.SpringBootWebTutorial.advices;


import com.module2.shubh.SpringBootWebTutorial.exceptions.ResourceNotFoundException;
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

    // Using custom exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException exception){
//        getting exception from the message
//        useful when we want to throw same expression with different error at multiple points within the application
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(apiError,HttpStatus.NOT_FOUND);
//        return new ResponseEntity<>("Resource not found", HttpStatus.NOT_FOUND);
    }
}
