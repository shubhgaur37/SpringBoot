package com.Module3.Practice.CollegeManagement.dto.student;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Terminal Student representation for 1:1 or Many-to-Many nesting.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentSummaryDTO {
    Long id;
    String name;
}