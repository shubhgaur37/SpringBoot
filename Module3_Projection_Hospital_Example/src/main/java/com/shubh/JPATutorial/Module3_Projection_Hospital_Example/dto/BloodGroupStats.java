package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BloodGroupStats {
    final BloodGroupType bloodGroupType;
    final Long count;
}
