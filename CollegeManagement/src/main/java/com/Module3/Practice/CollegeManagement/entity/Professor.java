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
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50)
    String name;

    // Parent side of relationship with subjects.
    // If a professor is deleted, their subjects are also removed (Orphan Removal).
    @OneToMany(mappedBy = "professor", orphanRemoval = true, cascade = CascadeType.ALL)
    Set<Subject> subjects = new HashSet<>();

    /*
     * REMOVED: Set<Student> students
     *
     * WHY THIS WAS DELETED:
     * 1. REDUNDANCY: A Professor is already linked to Students via the Subject entity.
     *    Maintaining a direct link creates two separate ways to store the same fact.
     * 2. DATA ANOMALIES: If you remove a student from a Subject but forget to remove
     *    them from the Professor's "students" set, your system will report that the
     *    Professor still teaches them, which is false.
     * 3. DATABASE NORMALIZATION: By removing this, we delete the 'professor_student_map'
     *    table entirely. We now rely on the 'subject_student' join table to determine
     *    the relationship.
     * 4. TRANSITIVE RELATIONSHIP: The logic is now: Professor -> Subject -> Student.
     */
}