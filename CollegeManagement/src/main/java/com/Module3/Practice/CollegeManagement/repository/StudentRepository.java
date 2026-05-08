package com.Module3.Practice.CollegeManagement.repository;

import com.Module3.Practice.CollegeManagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}