package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}