package com.Shubh.Module7.M7_TestingMethodologies.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckControllerBeanStalk {
    @GetMapping("/")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Application is running!");
    }

}