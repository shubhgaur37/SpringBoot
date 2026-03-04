package com.module2.shubh.SpringBootWebTutorial.service;

import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity;
import com.module2.shubh.SpringBootWebTutorial.repositories.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

// Uses @Component behind the scenes which makes it a bean
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
//    Using Model Mapper Defined in the Configuration class for converting entities to DTO's
    final ModelMapper modelMapper;

    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }
//    Instead of returning the entity, best practice is to return DTO of Employee by using a model mapper
//    between entity and dto
    public EmployeeDTO findById(Long id) {
//        map the entity object to DTO
        return modelMapper.map(employeeRepository.findById(id).orElse(null), EmployeeDTO.class);
    }

    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeEntity> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
    }
    public EmployeeDTO createNewEmployee(EmployeeDTO inputEmployee) {
        EmployeeEntity toSave = modelMapper.map(inputEmployee, EmployeeEntity.class);
//        Adding the employee to database
        return modelMapper.map(employeeRepository.save(toSave), EmployeeDTO.class);
    }
}
