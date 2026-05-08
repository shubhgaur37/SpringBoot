package com.Module3.Practice.CollegeManagement.controller;

import com.Module3.Practice.CollegeManagement.dto.subject.SubjectResponseDTO;
import com.Module3.Practice.CollegeManagement.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ARCHITECTURAL DESIGN: SUBJECT CONTROLLER
 * * 1. THE FLIP-SIDE RECURSION GUARD:
 * When fetching a Subject, the SubjectResponseDTO uses a ProfessorSummaryDTO.
 * This allows the client to see the Professor's name/ID without triggering
 * a recursive loop back into the Professor's full subject list.
 * <p>
 * 2. RELATIONSHIP MANAGEMENT:
 * Subjects are the "Bridge" in this system, linking Professors to Students.
 * These PatchMappings handle the specific assignment logic for those links.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping(path = "/{subjectId}")
    public ResponseEntity<SubjectResponseDTO> getSubjectById(@PathVariable Long subjectId) {
        return subjectService.getSubjectById(subjectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ACHIEVES: Professor Reassignment (Mandatory Link).
     * Updates the 'Owning Side' of the relationship.
     * Since Subject holds the Foreign Key (professor_id), patching this
     * resource is the cleanest way to move a subject to a different teacher.
     */
    @PatchMapping("/{subjectId}/professors/{professorId}")
    public ResponseEntity<SubjectResponseDTO> assignSubjectToProfessor(@PathVariable Long subjectId, @PathVariable Long professorId) {
        return subjectService.assignSubjectToProfessor(subjectId, professorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ACHIEVES: Student Enrollment (Optional Link).
     * Manages the Many-to-Many join table between Subjects and Students.
     * * NOTE: Unlike the Professor link, a Subject can exist with zero students.
     * This endpoint allows for dynamic growth of the class roster.
     */
    @PatchMapping("/{subjectId}/students/{studentId}")
    public ResponseEntity<SubjectResponseDTO> assignSubjectToStudent(@PathVariable Long subjectId, @PathVariable Long studentId) {
        return subjectService.assignSubjectToStudent(subjectId, studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}