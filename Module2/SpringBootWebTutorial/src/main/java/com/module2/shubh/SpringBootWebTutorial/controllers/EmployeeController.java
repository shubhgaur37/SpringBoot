package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity;
import com.module2.shubh.SpringBootWebTutorial.repositories.EmployeeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {
//    bad practice for controller to directly communicate with
//    persistence layer[DB], best practice is to use a service layer in between
//    for demonstration purposes
    EmployeeRepository employeeRepository;

    EmployeeController(EmployeeRepository employeeRepository) {
//        will be automatically injected as Employee Repository internally is a bean
        this.employeeRepository = employeeRepository;
    }

    @GetMapping(path = "/{employeeId}")
//    bad practice: revisited
    public EmployeeEntity getEmployeeByID(@PathVariable(name = "employeeId") Long id) {
//        returns an optional object, which helps us deal with null pointer exceptions
//        in a graceful way using methods specifying the exact steps to take
//        to deal with such exceptions
        return employeeRepository.findById(id).orElse(null);
    }

    @GetMapping
    public List<EmployeeEntity> getAllEmployees() {
        return employeeRepository.findAll();
    }

//    Request Body makes sure to map the request correctly with the dto after matching
//    the fields passed in the request.
    @PostMapping
    public EmployeeEntity createNewEmployee(@RequestBody EmployeeEntity inputEmployee) {
        return employeeRepository.save(inputEmployee);
    }

    @PutMapping
    public String updateEmployeeById(){
        return "HELLO from PUT";
    }

}

