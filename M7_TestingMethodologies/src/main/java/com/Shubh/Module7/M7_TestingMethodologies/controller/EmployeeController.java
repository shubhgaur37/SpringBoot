package com.Shubh.Module7.M7_TestingMethodologies.controller;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.service.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/employees")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {
    EmployeeService employeeService;

    @GetMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeByID(@PathVariable(name = "employeeId") Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createNewEmployee(@RequestBody EmployeeDTO inputEmployee) {
        EmployeeDTO savedEmployee = employeeService.createNewEmployee(inputEmployee);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    @PutMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployeeById(@PathVariable(name = "employeeId") Long id, @RequestBody EmployeeDTO updateEmployee) {
        return ResponseEntity.ok(employeeService.updateEmployeeByID(id, updateEmployee));
    }

    @DeleteMapping(path = "/{employeeId}")
    public ResponseEntity<Boolean> deleteEmployeeById(@PathVariable(name = "employeeId") Long id) {
        return ResponseEntity.ok(employeeService.deleteEmployeeByID(id));
    }
}
