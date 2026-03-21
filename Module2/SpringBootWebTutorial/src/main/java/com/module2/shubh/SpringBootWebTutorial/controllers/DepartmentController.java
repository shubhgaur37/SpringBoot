package com.module2.shubh.SpringBootWebTutorial.controllers;

import com.module2.shubh.SpringBootWebTutorial.dto.DepartmentDTO;
import com.module2.shubh.SpringBootWebTutorial.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/departments")
@RestController
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // API's

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping(path = "/{departmentId}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable("departmentId") Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody @Valid DepartmentDTO inputDepartment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(inputDepartment));
    }

    @PutMapping(path = "/{departmentId}")
    public ResponseEntity<DepartmentDTO> updateDepartmentById(@PathVariable("departmentId") Long id, @RequestBody @Valid DepartmentDTO updateDepartment) {
        return ResponseEntity.ok(departmentService.updateDepartmentById(id, updateDepartment));
    }

    @PatchMapping(path = "/{departmentId}")
    public ResponseEntity<DepartmentDTO> updatePartialDepartmentById(@PathVariable("departmentId") Long id, @RequestBody Map<String, Object> updateDepartmentPartial) {
        return ResponseEntity.ok(departmentService.updatePartialDepartmentById(id, updateDepartmentPartial));
    }

    @DeleteMapping(path = "/{departmentId}")
    public ResponseEntity<Boolean> deleteDepartmentById(@PathVariable("departmentId") Long id) {
        return ResponseEntity.ok(departmentService.deleteDepartmentById(id));
    }

}
