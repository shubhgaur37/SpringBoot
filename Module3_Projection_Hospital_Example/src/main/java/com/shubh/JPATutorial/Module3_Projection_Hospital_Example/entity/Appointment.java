package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    LocalDateTime appointmentTime;

    @Column(length = 500)
    String reason;

    String status;
//    A patient can have multiple appointments: One to Many from Patients to Appointment
//    One Appointment can only belong to a single patient and converse is true for appointments
//    i.e. Many appointments can belong to one patient, so we use @ManyToOne here
//    owning-side: patient_id as the foreign key.
//    Meaning Appointment cannot exist without the patient so Appointment becomes the owning side
    @ManyToOne
    // setting it to non-nullable, meaning appointment can only be created with a patient
    // enforces using a non-null constraint that patient should exist in the db before scheduling the appointment
    @JoinColumn(nullable = false)
    Patient patient;

    // Similarly relation-mapping for doctor

    @ManyToOne
    // setting it to non-nullable, meaning appointment can only be created with a doctor
    // enforces using a non-null constraint that doctor should exist in the db before scheduling the appointment
    @JoinColumn(nullable = false)
    Doctor doctor;

}