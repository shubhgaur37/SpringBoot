package com.shubh.module5.Spring_Security_Demo.advice;

import com.shubh.module5.Spring_Security_Demo.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception) {
        ApiError apiError = new ApiError(exception.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(Exception exception) {
        ApiError apiError = new ApiError(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }
}
