package com.module2.shubh.SpringBootWebTutorial.advices;


import com.module2.shubh.SpringBootWebTutorial.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

// annotation used to define a global exception handler
// applies globally to all controllers
// ensures proper exception handling mechanism is trigerred based on the type of exception
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Using custom exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException exception){
//        getting exception from the message
//        useful when we want to throw exceptions with custom error messages at multiple points within the application
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(apiError,HttpStatus.NOT_FOUND);
//        return new ResponseEntity<>("Resource not found", HttpStatus.NOT_FOUND);
    }

    // Handle all other exceptions except Resource Not Found
    // in case of Resource not found the control will go to first handler as it is more granular
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternalServerError(Exception exception){
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(apiError,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Exception Handler for invalid inputs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInputValidationErrors(MethodArgumentNotValidException exception){
        // get list of all the errors based on the error messages defined on the validations
        // getBinding Results
        List<String> errors = exception.getBindingResult().getAllErrors()
                .stream()
                .map(error ->error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                // generic error message
                .message("VALIDATION ERROR")
                // to return list of suberrors in response
                .subErrors(errors)
                .build();
        return new ResponseEntity<>(apiError,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
