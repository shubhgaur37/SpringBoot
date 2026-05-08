package com.Module3.Practice.CollegeManagement.controller;

import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorResponseDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectRequestDTO;
import com.Module3.Practice.CollegeManagement.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RECURSION SOLUTION (OPTION 2: SUMMARY DTOs)
 * * 1. MODEL MAPPING BENEFIT:
 * Using SubjectSummaryDTO instead of SubjectResponseDTO in the ProfessorResponse
 * prevents ModelMapper from entering an infinite loop. Since the SummaryDTO
 * has no 'professor' field, the mapping engine has a defined "stop point."
 * <p>
 * 2. SERIALIZATION BENEFIT:
 * Jackson (JSON provider) only serializes fields present in the DTO. By physically
 * removing the back-reference field in the Summary class, we eliminate the
 * "Circular Reference" path, resulting in a clean, flat JSON response.
 */
@RestController
@RequestMapping("/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    /**
     * DESIGN: DTO FIREWALL ACTIVATION
     * The @Valid annotation triggers Jakarta Bean Validation.
     * If the ProfessorRequestDTO violates constraints (e.g., empty subject list),
     * Spring rejects the request with a 400 Bad Request before calling the service.
     */
    @PostMapping
    public ResponseEntity<ProfessorResponseDTO> createProfessor(@Valid @RequestBody ProfessorRequestDTO professorDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professorService.createProfessor(professorDto));
    }

    /**
     * ACHIEVES: One-way data flow.
     * The response contains the new subjects, but those subjects do not
     * attempt to re-serialize the professor, saving bandwidth and preventing crashes.
     * * NOTE: @Valid on the List ensures that each SubjectRequestDTO title
     * is validated even in a bulk-add scenario.
     */
    @PostMapping("/{professorId}/subjects")
    public ResponseEntity<ProfessorResponseDTO> addNewSubjectsToProfessor(
            @PathVariable Long professorId,
            @Valid @RequestBody List<SubjectRequestDTO> subjectDtos) {

        return professorService.assignNewSubjectsOnly(professorId, subjectDtos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProfessorResponseDTO>> getAllProfessors() {
        List<ProfessorResponseDTO> professors = professorService.getAllProfessors();
        return ResponseEntity.ok(professors);
    }

    @GetMapping("/{professorId}")
    public ResponseEntity<ProfessorResponseDTO> getProfessorById(@PathVariable Long professorId) {
        return professorService.getProfessorById(professorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * TESTS: CASCADE DELETE
     * Deleting the Professor entity will trigger a cascade delete of all
     * associated Subject entities because of CascadeType.ALL.
     */
    @DeleteMapping("/{professorId}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long professorId) {
        professorService.deleteProfessor(professorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ACHIEVES: Subject Removal (Orphan Removal).
     * * DESIGN DECISION: Why DELETE instead of PATCH?
     * Although we are technically modifying the Professor's collection, the
     * business logic and DB constraints dictate that a Subject cannot exist
     * without a Professor. Therefore, removing the link results in the
     * destruction of the Subject record.
     * * Following REST semantics: Deleting a sub-resource link that results
     * in record deletion is best represented by the DELETE verb.
     */
    @DeleteMapping("/{professorId}/subjects/{subjectId}")
    public ResponseEntity<ProfessorResponseDTO> removeSubjectFromProfessor(
            @PathVariable Long professorId,
            @PathVariable Long subjectId) {

        return professorService.removeSubject(professorId, subjectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}