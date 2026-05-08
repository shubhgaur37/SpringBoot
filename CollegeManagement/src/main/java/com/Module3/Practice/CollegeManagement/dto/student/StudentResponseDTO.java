package com.Module3.Practice.CollegeManagement.dto.student;

import com.Module3.Practice.CollegeManagement.dto.admission_record.AdmissionRecordSummaryDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectSummaryDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * ACHIEVES: Full student profile.
 * - Uses List for API compatibility and predictable ordering.
 * - Breaks the 1:1 recursion with AdmissionRecord via Summary DTO.
 * - Breaks M:N recursion with Subject via SubjectSummaryDTO.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentResponseDTO {
    Long id;
    String name;

    // Entity uses Set, but DTO uses List for frontend ease-of-use
    Set<SubjectSummaryDTO> subjects;

    AdmissionRecordSummaryDTO admissionRecord;
}