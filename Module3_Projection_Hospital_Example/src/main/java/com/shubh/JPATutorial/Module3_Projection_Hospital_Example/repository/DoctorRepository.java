package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}