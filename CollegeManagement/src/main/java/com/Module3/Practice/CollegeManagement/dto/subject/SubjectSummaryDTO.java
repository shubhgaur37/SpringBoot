package com.Module3.Practice.CollegeManagement.dto.subject;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Terminal Subject representation.
 * Used inside ProfessorResponseDTO to list subjects without recursion.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubjectSummaryDTO {
    Long id;
    String title;
}