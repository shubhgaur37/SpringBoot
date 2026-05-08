package com.Module3.Practice.CollegeManagement.controller;

import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * NOTE ON SCOPE:
     * This controller manages the Student lifecycle and their general profile.
     * * LIMITATIONS:
     * - It does NOT support financial administrative tasks such as updating fees.
     * - Even though Student owns the 'AdmissionRecord' relationship, financial updates
     * should ideally be handled by a dedicated AdmissionController in the future
     * to ensure better separation of concerns (Registrar vs. Finance).
     */

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping(path = "/{studentId}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO studentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.createStudent(studentRequest));
    }

    @DeleteMapping(path = "/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}