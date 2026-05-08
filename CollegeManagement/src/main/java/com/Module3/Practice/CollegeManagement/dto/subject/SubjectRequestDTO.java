package com.Module3.Practice.CollegeManagement.dto.subject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * ACHIEVES: Captures intent to create a subject or enroll existing students.
 * Uses studentIds (Long) to avoid passing full Student objects in requests.
 * * --- DOMAIN LOGIC: RELATIONSHIP CARDINALITY ---
 * 1. MANDATORY (1..*): This DTO is typically nested within ProfessorRequestDTO,
 * ensuring every Subject is born with a Professor (The Owning Side).
 * 2. OPTIONAL (0..*): studentIds is not annotated with @NotEmpty because a
 * Subject can exist before any students enroll. Enrollment is a
 * separate lifecycle event.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubjectRequestDTO {

    /**
     * The unique identifying name of the course.
     * Validated at the DTO layer to ensure no "Untitled" subjects enter the system.
     */
    @NotBlank(message = "Subject title is required")
    String title;

    /**
     * List of existing Student IDs to link/enroll.
     * OPTIONAL: Can be null or empty if the subject is being created
     * without immediate student enrollment.
     */
    List<Long> studentIds;
}