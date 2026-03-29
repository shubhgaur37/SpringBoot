package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Patient.
 * Lombok annotations handle getters, setters, and private field defaults.
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false,length = 50)
    String name;

    @Column(nullable = false)
    LocalDate dob;

    @Column(unique = true)
    String email;

    /**
     * EnumType.STRING saves the name (e.g., "A_POSITIVE") rather than the index.
     * Prevents data misalignment if the enum order changes in the code.
     */
    @Enumerated(value = EnumType.STRING)
    BloodGroupType bloodGroup;

    /**
     * Automatically sets the timestamp on initial row creation.
     */
    @CreationTimestamp
    LocalDateTime createdAt;

    /**
     * Bi Directional Mapping with insurance
     * One-to-One mapping where Patient is the 'Owning Side' (holds the foreign key).
     *
     * @JoinColumn details: to be applied on the owning side, table where foreign key is defined
     * - name: Specifies the physical column name in the Patient table.
     * - unique = true: Explicitly creates a UNIQUE constraint in the DB schema. Creates an index automatically for columns with unique constraint.
     *   taken care by @OneToOne Annotation automatically but just a safeguard and best practice to also include it.
     *   Note: While @OneToOne implies a 1:1 link, 'unique=true' ensures the DB
     *   physically prevents multiple Patients from sharing the same Insurance record.
     */
    @OneToOne
    @JoinColumn(name = "patient_insurance", unique = true)
    Insurance insurance;

    @OneToMany(mappedBy = "patient") // inverse-side
    Set<Appointment> appointments = new HashSet<>();
}
