package com.Module3.Practice.CollegeManagement.dto.professor;

import com.Module3.Practice.CollegeManagement.dto.subject.SubjectSummaryDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * ACHIEVES: A detailed view of the Professor including their subjects.
 * To avoid recursion, it uses SubjectSummaryDTO which does not point back to the Professor.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProfessorResponseDTO {
    Long id;
    String name;

    Set<SubjectSummaryDTO> subjects;
}