package com.Module3.Practice.CollegeManagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = true)
    String title;

    /**
     * OWNING SIDE (Many-to-One):
     * This column holds the Foreign Key to the Professor table.
     * Note: 'unique = true' is omitted here to allow one professor to teach multiple subjects.
     */
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    Professor professor;

    /**
     * OWNING SIDE (Many-to-Many):
     * We define the Join Table here.
     * <p>
     * THE "POINTING" RULES:
     * 1. joinColumns: Always points to the ID of the CURRENT class (Subject).
     * Think: "Join from here."
     * 2. inverseJoinColumns: Always points to the ID of the OTHER class (Student).
     * Think: "Join to the partner."
     * <p>
     * The 'name' attribute is a CUSTOM LABEL you choose for the DB column.
     * Hibernate will create these columns in the DB using these exact names.
     */
    @ManyToMany
    @JoinTable(
            name = "subject_student_mapping",
            joinColumns = @JoinColumn(name = "subject_id"),        // Points to Subject's ID
            inverseJoinColumns = @JoinColumn(name = "student_id")  // Points to Student's ID
    )
    Set<Student> students = new HashSet<>();
}
