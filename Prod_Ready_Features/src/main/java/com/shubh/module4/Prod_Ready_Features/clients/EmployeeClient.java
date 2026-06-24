package com.shubh.module4.Prod_Ready_Features.clients;

import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeClient {

    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO createNewEmployee(EmployeeDTO inputDTO);
}
