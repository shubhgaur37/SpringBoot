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

//    Defines a mapping between path parameter and method argument
//    if we want to use different names for both of them
    @GetMapping(path = "/{employeeId}")
    public EmployeeDTO getEmployeeByID(@PathVariable(name = "employeeId") long id) {
        return new EmployeeDTO(id,"Shubh","shubh@gaur.com",26, LocalDate.now(),true);
    }
    //    Defines a mapping between optional query parameter and method argument
    //    enforces name constraint on the query parameter's name
    //    Since query parameters are optional, if we try to use an undefined parameter. eg. xyz=hello
    //    then also we will get a 200 response because the parameter passed doesn't exist in the method arguments.
    //    Also, its not required for us to add a / because the convention for query params is
    //    resource_name?optional_query_params
    @GetMapping
    public String getAllEmployees(@RequestParam(required = false,name = "inputAge") Integer age, @RequestParam(required = false) String sortBy) {
        return "Hi, Age = " + age + " | "  + sortBy;
    }

//    Request Body makes sure to map the request correctly with the dto after matching
//    the fields passed in the request.
    @PostMapping
    public EmployeeDTO createEmployee(@RequestBody EmployeeDTO inputEmployee) {
        inputEmployee.setId(37);
        return inputEmployee;
    }

    @PutMapping
    public String updateEmployeeById(){
        return "HELLO from PUT";
    }

}

