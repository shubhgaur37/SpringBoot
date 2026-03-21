package com.module2.shubh.SpringBootWebTutorial.exceptions;

// Custom Exception
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
