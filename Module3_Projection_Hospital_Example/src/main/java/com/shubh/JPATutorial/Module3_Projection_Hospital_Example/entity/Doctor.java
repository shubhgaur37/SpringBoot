package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Doctor.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(length = 100)
    String specialization;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    @CreationTimestamp
    LocalDateTime createdAt;

    /**
     * Relationship Concepts: Unidirectional vs Bidirectional Mapping
     *
     * 1. Unidirectional (One-Way):
     *    - Only one entity knows about the other in Java code.
     *    - If this Doctor class did NOT have this 'appointments' field, the
     *      relationship would be Unidirectional from Appointment -> Doctor.
     *    - You could call appointment.getDoctor(), but NOT doctor.getAppointments().
     *
     * 2. Bidirectional (Two-Way):
     *    - Both entities have fields referring to each other in Java code.
     *    - By adding this 'appointments' field here and using 'mappedBy', we make
     *      it Bidirectional.
     *    - Now you can navigate the relationship from both sides in Java:
     *      doctor.getAppointments() AND appointment.getDoctor().
     *
     * 3. Behavior of mappedBy:
     *    - Without 'mappedBy': Hibernate assumes a Unidirectional mapping and
     *      tries to manage the link (Owning-side) by creating a Join Table.
     *    - With 'mappedBy': Signals this is the Inverse-side. It points to the
     *      field in the Appointment class that manages the link (Owning-side).
     */
    @OneToMany(mappedBy = "doctor")
    Set<Appointment> appointments = new HashSet<>();
}
