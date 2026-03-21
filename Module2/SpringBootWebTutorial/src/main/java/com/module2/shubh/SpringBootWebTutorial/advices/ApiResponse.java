package com.module2.shubh.SpringBootWebTutorial.advices;


import lombok.Getter;

import java.time.LocalDateTime;

// API Response Transformation
// default body that we wanna use for our responses
// either data will be there or error
@Getter
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private T data;
    private ApiError error;

    public ApiResponse(){
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(T data){
        this(); // calling default constructor
        this.data = data;
    }

    public ApiResponse(ApiError error){
        this();
        this.error = error;
    }

}


