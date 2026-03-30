package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
}