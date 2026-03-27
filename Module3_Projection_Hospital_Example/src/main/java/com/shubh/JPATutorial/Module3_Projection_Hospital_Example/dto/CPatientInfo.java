package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CPatientInfo {
    // final fields picked up by requiredArgsConstructor
    final Long id;
    final String name;
}
