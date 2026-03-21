package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.module2.shubh.SpringBootWebTutorial.service.EmployeeService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
//    ResponseEntity are used in order to return responses along with status codes
    @GetMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeByID(@PathVariable(name = "employeeId") Long id) {
//        if some entity exists then return it in the response with appropriate code else return not found
        return employeeService.findById(id)
                .map(employeeDTO -> ResponseEntity.ok(employeeDTO))
                // supplier required in arguments so using a lambda function
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        // whenever this exception is thrown, control goes to corresponding exception handler
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

//    Request Body makes sure to map the request correctly with the dto after matching
//    the fields passed in the request.
//    Asking the controller to validate the request before servicing it usin Jakarta's @Valid annotation with request
    @PostMapping
    public ResponseEntity<EmployeeDTO> createNewEmployee(@RequestBody @Valid EmployeeDTO inputEmployee) {
        EmployeeDTO savedEmployee = employeeService.createNewEmployee(inputEmployee);
//        new ResponseEntity<>(employeeDTO, HttpStatus.CREATED); // can be used alternatively
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

    }

//    When we want to change an entire record(row) in the db then we use a put request signalling,
//    we want to update an entire row
//    also the request incorrectly updates the record
//    if all the fields are not provided i.e. the request body does not
//    follow the rules of a put request
    @PutMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployeeById(@PathVariable(name="employeeId") Long id, @RequestBody @Valid EmployeeDTO updateEmployee) {
        return ResponseEntity.ok(employeeService.updateEmployeeByID(id,updateEmployee));
    }

    @DeleteMapping(path = "/{employeeId}")
    public ResponseEntity<Boolean> deleteEmployeeById(@PathVariable(name="employeeId") Long id) {
        boolean isEmployeeDeleted = employeeService.deleteEmployeeByID(id);
        if(isEmployeeDeleted) return ResponseEntity.ok(isEmployeeDeleted);
        return ResponseEntity.notFound().build();
    }
//    We want to update some fields within the table and we might not know what all fields came in the request
//    maybe some invalid fields may cause mapping issues with DTO
//    so instead of a DTO we are using a map with key as strings denoting fields we want to update
//    Error :java.lang.IllegalArgumentException: Can not set java.time.LocalDate field com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity.dateOfJoining to java.lang.String
//    and corresponding objects denoting the values that we wanna set for these fields
    @PatchMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeDTO> updatePartialEmployeeById(@PathVariable(name="employeeId") Long id, @RequestBody Map<String,Object> partialEmployee) {
        EmployeeDTO employeeDTO = employeeService.updatePartialEmployeeByID(id,partialEmployee);
        if (employeeDTO == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(employeeDTO);
    }

//    Exception handling to prevent application crashes and meaningful error responses
//    for end users, facilitate debugging and maintenance, ensure consistent error handling across the application
    @ExceptionHandler(NoSuchElementException.class) // defined in controller to handle controller level exceptions
    // also we are getting a 200 response which is not consistent with the actual state, so lets return a proper response entity
    // from handler itself
    public ResponseEntity<String> handleEmployeeNotFound(NoSuchElementException exception){
        return new ResponseEntity<>("Employee was not found", HttpStatus.NOT_FOUND);
    }

}

// We were able to get rid of the Employee Repository by introducing a Service layer in between to interact with database(repository) which
// a good practice as we don't want direct interaction of db with clients and only allow service to communicate with it.
// In this way, we can abstract out complex validation, intermediate steps from the presentation or client facing layer
