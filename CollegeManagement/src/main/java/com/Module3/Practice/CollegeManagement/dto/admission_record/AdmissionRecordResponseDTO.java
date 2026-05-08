package com.Module3.Practice.CollegeManagement.dto.admission_record;

import com.Module3.Practice.CollegeManagement.dto.student.StudentSummaryDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Detailed Admission Record view.
 * Includes StudentSummaryDTO to break the 1:1 link back to AdmissionRecord.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdmissionRecordResponseDTO {
    Long id;
    Integer fees;

    // Path: Admission -> StudentSummary (Relationship Capped)
    StudentSummaryDTO student;
}