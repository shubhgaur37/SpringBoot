package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * REVISION SUMMARY: Relationship Management in Department Entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String name;

    @CreationTimestamp
    LocalDateTime createdAt;

    /**
     * RELATIONSHIP 1: One-to-One (Head of Department)
     * Type: Unidirectional (Doctor does not have a 'headedDepartment' field).
     * <p>
     * OWNING SIDE: Department.
     * - Why? Because it contains the @JoinColumn.
     * - DB Result: The 'Department' table will have a physical column named "Head".
     * <p>
     * CONSTRAINTS:
     * - nullable = false: You cannot create a Department without assigning an existing Doctor.
     * - Identity: Since it's @OneToOne, one Doctor can only head one Department.
     */
    @OneToOne
    @JoinColumn(nullable = false, name = "Head")
    Doctor headDoctor;

    /**
     * RELATIONSHIP 2: Many-to-Many (Staff Doctors)
     * Type: Unidirectional (Doctor does not have a 'departments' list).
     * <p>
     * OWNING SIDE: Department.
     * - Why? Because 'mappedBy' is absent here. This entity "owns" the association.
     * - DB Result: Hibernate creates a JOIN TABLE (e.g., department_doctors)
     * automatically to map IDs from both tables.
     * <p>
     * DESIGN CHOICE:
     * - No Bidirectional mapping is needed in the Doctor entity unless your
     * business logic specifically requires calling 'doctor.getDepartments()'.
     * - Keeping it Unidirectional here keeps the Doctor entity "clean" and
     * prevents unnecessary memory overhead.
     */
    @ManyToMany
    Set<Doctor> doctors = new HashSet<>();
}
