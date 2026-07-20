package com.Shubh.Module7.M7_TestingMethodologies.service;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {
    EmployeeDTO getEmployeeById(Long id);

    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO createNewEmployee(EmployeeDTO inputEmployee);

    EmployeeDTO updateEmployeeByID(Long id, EmployeeDTO updateEmployee);

    boolean deleteEmployeeByID(Long id);
}
