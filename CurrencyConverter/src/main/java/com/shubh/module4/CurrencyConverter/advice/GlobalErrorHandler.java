package com.shubh.module4.CurrencyConverter.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException exception){
        ApiError error = new ApiError(exception.getLocalizedMessage());
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }
}
