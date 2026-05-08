package com.Module3.Practice.CollegeManagement.dto.professor;

import com.Module3.Practice.CollegeManagement.dto.subject.SubjectRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * ARCHITECTURAL DESIGN: PROFESSOR REQUEST DTO
 * * 1. VALIDATION AT THE DTO LAYER (Fail-Fast):
 * This is a "New Concept" for this project. Instead of allowing malformed data
 * into the Service layer, we use Jakarta Validation (Bean Validation) to
 * reject bad requests at the Controller entry-point.
 * <p>
 * 2. MANDATORY 1..* RELATIONSHIP:
 * By combining @NotEmpty and @Valid, we enforce that a Professor cannot
 * exist without at least one valid Subject.
 * <p>
 * --- EXAMPLE: NESTED VALIDATION FAILURE ---
 * If a user sends a valid Professor name but an invalid Subject title:
 * Request: { "name": "Dr. Smith", "subjects": [{ "title": "" }] }
 * <p>
 * Response (400 Bad Request):
 * {
 * "field": "subjects[0].title",
 * "rejectedValue": "",
 * "message": "Subject title cannot be blank"
 * }
 * Note: Without @Valid on the list, the blank title would be ignored.
 */
@Data
public class ProfessorRequestDTO {

    @NotBlank(message = "Professor name cannot be blank")
    private String name;

    /**
     * CONSTRAINT: At least one subject required.
     * * @NotEmpty: Ensures the list is not null AND not empty ([]).
     *
     * @Valid: Tells Spring to perform nested validation. Without this, Spring
     * would check that the list exists, but wouldn't check if the Subject titles
     * inside the list are blank.
     */
    @NotEmpty(message = "A professor must be assigned at least one subject.")
    @Valid
    private List<SubjectRequestDTO> subjects;

    /*
     * ARCHITECTURAL REMOVAL: Set<StudentDTO> students
     *
     * WHY THIS WAS REMOVED:
     * 1. DOMAIN ALIGNMENT: The Professor entity no longer has a 'students' field.
     * DTOs must mirror the domain constraints.
     * 2. N+1 QUERY PREVENTION: Fetching students through subjects inside a
     * generic DTO mapping causes significant performance degradation
     * (one query for the prof, one for subjects, and X queries for students).
     * 3. SRP (Single Responsibility): Keeping the Professor profile lean.
     * Detailed student rosters should be handled by specialized endpoints.
     */
}