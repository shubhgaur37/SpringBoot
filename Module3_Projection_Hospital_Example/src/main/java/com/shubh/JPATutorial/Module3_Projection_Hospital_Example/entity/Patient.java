package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data // generates required args constructor and toString, hashcode, equals
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    LocalDate dob;

    String email;

    // Jakarta(JPA): tells JPA to use strings for this enum instead of ordinals
    // important because if in the future the order of enum values change, then ordering breaks
    // in DB, if EnumType.Ordinal is being used
    @Enumerated(value = EnumType.STRING)
    BloodGroupType bloodGroup;

    @CreationTimestamp
    LocalDateTime createdAt;
}

