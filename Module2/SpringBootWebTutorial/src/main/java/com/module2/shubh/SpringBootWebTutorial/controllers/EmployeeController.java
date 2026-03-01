package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
public class EmployeeController {

    // Defining a path parameter in resource endpoint
    // Marking the argument in method using @PathVariable annotation that will use the path param's value
    // Note(IMP): Always make sure that path param and function argument are exactly the same
    // Otherwise the method won't know which param to use.
    @GetMapping(path = "/employees/{employeeId}")
    public EmployeeDTO getEmployeeByID(@PathVariable long employeeId) {
        return new EmployeeDTO(employeeId,"Shubh","shubh@gaur.com",26, LocalDate.now(),true);
    }
}

