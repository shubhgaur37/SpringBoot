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
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50)
    String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false)
    AdmissionRecord admissionRecord;

    /*
     * REMOVED: Set<Professor> professors
     *
     * DANGERS OF THE REDUNDANT MANY-TO-MANY LINK:
     * 1. DATA INCONSISTENCY: If a student is manually linked to a professor but not
     *    enrolled in any of their subjects, the database "lies" about the relationship.
     * 2. SYNCHRONIZATION OVERHEAD: Every enrollment change would require updating two
     *    separate tables. Forgetting one creates "stale" data.
     * 3. INFINITE RECURSION: Standard JSON serializers (like Jackson) often crash with
     *    StackOverflowError when two entities reference each other directly in a cycle.
     * 4. VIOLATION OF DRY: The Student -> Subject -> Professor chain is the "Single
     *    Source of Truth." Derived links should be queried, not stored.
     */

    @ManyToMany(mappedBy = "students")
    Set<Subject> subjects = new HashSet<>();
}