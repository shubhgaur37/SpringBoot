package com.Module3.Practice.CollegeManagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Entity
public class AdmissionRecord {
    @Id
//    delegated to DB[Autoincrement]
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Integer fees;

    // inverse side
    // Analogy: "I don't keep the files here. Go look at the 'admissionRecord'
    // field inside the Student class to see how we are linked."
    @OneToOne(mappedBy = "admissionRecord")
    Student student;
}
