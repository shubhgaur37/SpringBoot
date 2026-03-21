package com.module2.shubh.SpringBootWebTutorial.repositories;

import com.module2.shubh.SpringBootWebTutorial.entities.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Part of Module 2 Practice

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

}
