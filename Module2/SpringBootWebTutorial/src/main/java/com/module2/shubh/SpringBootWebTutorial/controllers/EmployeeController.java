package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity;
import org.springframework.web.bind.annotation.*;
import com.module2.shubh.SpringBootWebTutorial.service.EmployeeService;

import java.util.List;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
//        will be automatically injected as Employee Service internally is a bean
        this.employeeService = employeeService;
    }

    @GetMapping(path = "/{employeeId}")
    public EmployeeEntity getEmployeeByID(@PathVariable(name = "employeeId") Long id) {
        return employeeService.findById(id);
    }

    @GetMapping
    public List<EmployeeEntity> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

//    Request Body makes sure to map the request correctly with the dto after matching
//    the fields passed in the request.
    @PostMapping
    public EmployeeEntity createNewEmployee(@RequestBody EmployeeEntity inputEmployee) {
        return employeeService.createNewEmployee(inputEmployee);
    }

    @PutMapping
    public String updateEmployeeById(){
        return "HELLO from PUT";
    }
}

// We were able to get rid of the Employee Repository by introducing a Service layer in between to interact with database(repository) which
// a good practice as we don't want direct interaction of db with clients and only allow service to communicate with it.
// In this way, we can abstract out complex validation, intermediate steps from the presentation or client facing layer
