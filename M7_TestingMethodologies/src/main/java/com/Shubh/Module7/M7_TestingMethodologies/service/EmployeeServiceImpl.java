package com.Shubh.Module7.M7_TestingMethodologies.service;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import com.Shubh.Module7.M7_TestingMethodologies.exception.DuplicateResourceException;
import com.Shubh.Module7.M7_TestingMethodologies.exception.ResourceNotFoundException;
import com.Shubh.Module7.M7_TestingMethodologies.repository.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    EmployeeRepository employeeRepository;
    ModelMapper modelMapper;

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee with id {}", id);
        return modelMapper.map(getEmployeeEntityById(id), EmployeeDTO.class);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findAll().stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .toList();
    }

    @Override
    public EmployeeDTO createNewEmployee(EmployeeDTO inputEmployee) {
        log.info("Creating employee with email {}", inputEmployee.getEmail());
        if (!employeeRepository.findByEmail(inputEmployee.getEmail()).isEmpty()) {
            log.error("Employee with email {} already exists", inputEmployee.getEmail());
            throw new DuplicateResourceException("Employee already exists with email: " + inputEmployee.getEmail());
        }
        Employee toSave = modelMapper.map(inputEmployee, Employee.class);
        Employee savedEmployee = employeeRepository.save(toSave);
        log.info("Created employee with id {}", savedEmployee.getId());
        return modelMapper.map(savedEmployee, EmployeeDTO.class);
    }

    @Override
    public EmployeeDTO updateEmployeeByID(Long id, EmployeeDTO updateEmployee) {
        log.info("Updating employee with id {}", id);
        Employee employee = getEmployeeEntityById(id);
        if (!updateEmployee.getEmail().equals(employee.getEmail())) {
            log.error("Attempted to update email for employee with id: {}", employee.getId());
            throw new RuntimeException("Email of the employee cannot be updated");
        }
        Employee toUpdate = modelMapper.map(updateEmployee, Employee.class);
        toUpdate.setId(id);
        Employee savedEmployee = employeeRepository.save(toUpdate);
        log.info("Updated employee with id {}", id);
        return modelMapper.map(savedEmployee, EmployeeDTO.class);
    }

    @Override
    public boolean deleteEmployeeByID(Long id) {
        log.info("Deleting employee with id {}", id);
        getEmployeeEntityById(id);
        employeeRepository.deleteById(id);
        log.info("Deleted employee with id {}", id);
        return true;
    }

    private Employee getEmployeeEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee not found with id {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });
    }
}
