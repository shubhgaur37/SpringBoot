package com.Module3.Practice.CollegeManagement.dto.professor;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: A "Leaf" node for Professor data.
 * Used when viewing a Subject to show who the teacher is without
 * triggering a fetch of all that teacher's other subjects.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfessorSummaryDTO {
    Long id;
    String name;
}