package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an Insurance record.
 * Uses Lombok for boilerplate reduction and Jakarta Persistence for mapping.
 */
@FieldDefaults(level = AccessLevel.PRIVATE) // Sets all class fields to 'private'
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    Long id;

    /**
     * Unique policy identifier.
     * nullable = false: Enforces NOT NULL in DB.
     * unique = true: Ensures no two records have the same policy number.
     * length = 50: Limits column width for optimization.
     */
    @Column(nullable = false, unique = true, length = 50)
    String policyNumber;

    @Column(nullable = false, length = 100)
    String provider;

    @Column(nullable = false)
    LocalDate validUntil;

    /**
     * Managed by Hibernate: sets timestamp once upon initial row creation.
     */
    @CreationTimestamp
    LocalDateTime createdAt;

    /**
     * Represents the Inverse Side of the One-to-One relationship.
     *
     * mappedBy = "insurance":
     * 1. Signals that the 'Patient' entity owns the relationship.
     * 2. Tells JPA to look at the 'insurance' field in the Patient Entity for mapping.
     * 3. Prevents a redundant foreign key column from being created in the Insurance table.
     *
     * This avoids data ambiguity by ensuring the Patient table remains the
     * Single Source of Truth for the relationship.
     */
    @OneToOne(mappedBy = "insurance")
    Patient patient;
}
