package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}