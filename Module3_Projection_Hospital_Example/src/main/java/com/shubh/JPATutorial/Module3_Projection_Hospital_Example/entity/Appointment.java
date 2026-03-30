package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
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

    /**
     * PITFALL: Missing @ToString.Exclude on LAZY associations.
     * If this entity is DETACHED (transaction closed) and toString() is called
     * (e.g., in a log or debugger), Hibernate will attempt to initialize the
     * Proxy. Since the session is closed, it throws LazyInitializationException.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(nullable = false)
    Patient patient;

    /**
     * PITFALL: Infinite Recursion.
     * If the Doctor entity also has a @ToString that includes a list of Appointments,
     * calling toString() on either will trigger a circular chain of calls,
     * resulting in a StackOverflowError.
     */
    // doctors are many to one, so default fetch type is eager
    // I have set it to lazy because I don't want the doctor's information when I fetch appointments for a patient
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(nullable = false)
    Doctor doctor;
}