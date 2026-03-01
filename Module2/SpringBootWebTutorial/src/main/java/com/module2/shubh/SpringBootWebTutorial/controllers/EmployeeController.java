package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {
//    Having to define the endpoint in every type of get request for this resource is extra work
//    Instead we can define a top level resource endpoint on the controller that makes sure
//    all requests for the employee resource are routed to appropriate methods within this controller
//    it also enables us to eliminate redundant definition of employee resource on the get mappings
//    In essence, /employees acts as a prefix for all the defined mappings
    @GetMapping(path = "/{employeeId}")
    public EmployeeDTO getEmployeeByID(@PathVariable long employeeId) {
        return new EmployeeDTO(employeeId,"Shubh","shubh@gaur.com",26, LocalDate.now(),true);
    }

    @GetMapping(path = "/")
    public String getAllEmployees(@RequestParam(required = false) Integer age, @RequestParam(required = false) String sortBy) {
        return "Hi, Age = " + age + " | "  + sortBy;
    }
}

