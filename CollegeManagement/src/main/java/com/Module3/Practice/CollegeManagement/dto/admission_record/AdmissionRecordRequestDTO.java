package com.Module3.Practice.CollegeManagement.dto.admission_record;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ACHIEVES: Input for financial records.
 * studentId is a writable Long so the service knows which student to link.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdmissionRecordRequestDTO {
    Long id;

    @NotNull(message = "Fees cannot be null")
    @PositiveOrZero(message = "Fees must be zero or positive")
    Integer fees;

    Long studentId;
}