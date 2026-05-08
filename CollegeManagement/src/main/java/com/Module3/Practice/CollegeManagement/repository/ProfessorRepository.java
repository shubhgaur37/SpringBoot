package com.Module3.Practice.CollegeManagement.repository;

import com.Module3.Practice.CollegeManagement.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}