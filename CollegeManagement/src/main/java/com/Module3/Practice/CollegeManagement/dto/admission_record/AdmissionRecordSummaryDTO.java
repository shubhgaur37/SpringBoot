package com.Module3.Practice.CollegeManagement.dto.admission_record;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Terminal view of admission data for nesting inside StudentResponseDTO.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdmissionRecordSummaryDTO {
    Long id;
    Integer fees;
}