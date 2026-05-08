package com.Module3.Practice.CollegeManagement.dto.subject;

import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorSummaryDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentSummaryDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * ACHIEVES: Detailed Subject view.
 * - Uses List for API consistency.
 * - Caps recursion by using Summary DTOs for both Professor and Students.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubjectResponseDTO {

    Long id;
    String title;

    // Terminal: Won't point back to Subjects
    ProfessorSummaryDTO professor;

    // Terminal: Won't point back to Student's full profile
    Set<StudentSummaryDTO> students;
}