package com.shubh.module4.Prod_Ready_Features.advice;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ApiError {
    LocalDateTime timestamp;
    String error;
    HttpStatus statusCode;

    public ApiError(){
        timestamp = LocalDateTime.now();
    }
    public ApiError(String error, HttpStatus statusCode){
        this();
        this.error = error;
        this.statusCode = statusCode;
    }
}
