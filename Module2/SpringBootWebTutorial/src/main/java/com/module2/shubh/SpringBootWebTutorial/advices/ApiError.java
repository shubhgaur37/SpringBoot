package com.module2.shubh.SpringBootWebTutorial.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

// DTO to pass error bodies when exceptions occur
@Data
@Builder
public class ApiError {

    private HttpStatus status;
    private String message;

}
