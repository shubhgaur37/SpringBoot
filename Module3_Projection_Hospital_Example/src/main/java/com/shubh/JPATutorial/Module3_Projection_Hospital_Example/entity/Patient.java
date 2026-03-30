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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50)
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
     * taken care by @OneToOne Annotation automatically but just a safeguard and best practice to also include it.
     * Note: While @OneToOne implies a 1:1 link, 'unique=true' ensures the DB
     * physically prevents multiple Patients from sharing the same Insurance record.
     * <p>
     * ---------------- CASCADING ----------------
     * <p>
     * Cascade defines whether operations performed on Patient should be
     * automatically propagated to the associated Insurance entity.
     * Good practice to define cascading on the owning side of relationship.
     * <p>
     * Example (if cascade is added):
     * @OneToOne(cascade = CascadeType.ALL)
     * <p>
     * Then:
     * - save(patient)   → insurance is also saved automatically
     * - delete(patient) → insurance is also deleted
     * - merge(patient)  → insurance is also updated
     * <p>
     * Without cascade (current setup):
     * - Insurance must be saved explicitly using insuranceRepository.save(...)
     * - Otherwise, setting a new (transient) Insurance may cause an error
     * because Hibernate cannot persist it automatically.
     * <p>
     * Important:
     * - Cascade does NOT control the relationship itself.
     * - Patient (owning side) still controls the foreign key in DB.
     */
//    @OneToOne(cascade = CascadeType.PERSIST)
//    Orphan removal ensures that whenever the insurance for a patient is updated, the older insurance mapping
//    is removed from the DB, as it no longer has a mapping. It can be done by setting orphanRemoval parameter
//    to true in the oneToOne annotation. can be done on parent as well as children side depending on the usecase
//    here it is being done for the parent side
//    Also, currently whenever we fetch a patient from the db we do a join call to fetch the corresponding insurance
//    this is because we have fetch type set to eager by default in one to one mapping, if we only want to fetch insurance
//    when we want to see the insurance then we can set the fetch type to lazy. This will also improve the query performance for
//    fetching patients
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    // multiple cascading types can also be defined together
    @JoinColumn(name = "patient_insurance", unique = true)
    Insurance insurance;

    // Whenever a patient is deleted appointments are also deleted
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL) // inverse-side
    @Builder.Default
    Set<Appointment> appointments = new HashSet<>();
}
