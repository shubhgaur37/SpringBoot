package com.Shubh.Module7.M7_TestingMethodologies.repository;

import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByEmail(String email);

}
