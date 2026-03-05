package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import org.springframework.web.bind.annotation.*;
import com.module2.shubh.SpringBootWebTutorial.service.EmployeeService;

import java.util.List;
import java.util.Map;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {
// Still, We are using EmployeeEntity for serving our requests which is a part of persistence layer
// Best Practice: Use a data transfer object(DTO) to serve responses through the presentation layer
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
//        will be automatically injected as Employee Service internally is a bean
        this.employeeService = employeeService;
    }

    @GetMapping(path = "/{employeeId}")
    public EmployeeDTO getEmployeeByID(@PathVariable(name = "employeeId") Long id) {
        return employeeService.findById(id);
    }

    @GetMapping
    public List<EmployeeDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

//    Request Body makes sure to map the request correctly with the dto after matching
//    the fields passed in the request.
    @PostMapping
    public EmployeeDTO createNewEmployee(@RequestBody EmployeeDTO inputEmployee) {
        return employeeService.createNewEmployee(inputEmployee);
    }

//    When we want to change an entire record(row) in the db then we use a put request signalling,
//    we want to update an entire row
//    also the request incorrectly updates the record
//    if all the fields are not provided i.e. the request body does not
//    follow the rules of a put request
    @PutMapping(path = "/{employeeId}")
    public EmployeeDTO updateEmployeeById(@PathVariable(name="employeeId") Long id, @RequestBody EmployeeDTO updateEmployee) {
        return employeeService.updateEmployeeByID(id,updateEmployee);
    }

    @DeleteMapping(path = "/{employeeId}")
    public boolean deleteEmployeeById(@PathVariable(name="employeeId") Long id) {
        return employeeService.deleteEmployeeByID(id);
    }
//    We want to update some fields within the table and we might not know what all fields came in the request
//    maybe some invalid fields may cause mapping issues with DTO
//    so instead of a DTO we are using a map with key as strings denoting fields we want to update
//    Error :java.lang.IllegalArgumentException: Can not set java.time.LocalDate field com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity.dateOfJoining to java.lang.String
//    and corresponding objects denoting the values that we wanna set for these fields
    @PatchMapping(path = "/{employeeId}")
    public EmployeeDTO updatePartialEmployeeById(@PathVariable(name="employeeId") Long id, @RequestBody Map<String,Object> partialEmployee) {
        return employeeService.updatePartialEmployeeByID(id,partialEmployee);
    }
}

// We were able to get rid of the Employee Repository by introducing a Service layer in between to interact with database(repository) which
// a good practice as we don't want direct interaction of db with clients and only allow service to communicate with it.
// In this way, we can abstract out complex validation, intermediate steps from the presentation or client facing layer
