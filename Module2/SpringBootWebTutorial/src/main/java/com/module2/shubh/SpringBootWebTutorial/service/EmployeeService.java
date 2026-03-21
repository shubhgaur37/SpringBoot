package com.module2.shubh.SpringBootWebTutorial.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.module2.shubh.SpringBootWebTutorial.dto.EmployeeDTO;
import com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity;
import com.module2.shubh.SpringBootWebTutorial.exceptions.ResourceNotFoundException;
import com.module2.shubh.SpringBootWebTutorial.repositories.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Uses @Component behind the scenes which makes it a bean
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    //    Using Model Mapper Defined in the Configuration class for converting entities to DTO's
    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper modelMapper, ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    //    Instead of returning the entity, best practice is to return DTO of Employee by using a model mapper
//    between entity and dto
//    We are getting internal server error caused by a null entity not being able to be mapped with dto
//    java.lang.IllegalArgumentException: source cannot be null
    public Optional<EmployeeDTO> findById(Long id) {
//        map the entity object to DTO
//        Optional<EmployeeEntity> employeeEntity = employeeRepository.findById(id);
//        if no entity is found then we return an empty optional
//        return employeeEntity.map(employee -> modelMapper.map(employee, EmployeeDTO.class));
//        Simplified version
        return employeeRepository.findById(id).map(employee -> modelMapper.map(employee, EmployeeDTO.class));
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

    public EmployeeDTO updateEmployeeByID(Long id, EmployeeDTO updateEmployee) {
        // if employee exists no exception thrown
        isEmployeeIDPresent(id);

        EmployeeEntity toUpdate = modelMapper.map(updateEmployee, EmployeeEntity.class);
//        if the employee already exists, then it updates the details
//        If the employee does not exist then it creates a new one
//        if (!isEmployeeIDPresent(id)) throw new ResourceNotFoundException("Employee not found with id: " + id);
        // the above exception will also be caught by global handler for handling

        toUpdate.setId(id);
        return modelMapper.map(employeeRepository.save(toUpdate), EmployeeDTO.class);

    }

    public boolean deleteEmployeeByID(Long id) {
        // if employee exists no exception thrown
        isEmployeeIDPresent(id);
        // duplicated
//        if (!employeeExists) throw new ResourceNotFoundException("Employee not found with id: " + id);
        employeeRepository.deleteById(id);
        return true;

    }

    public EmployeeDTO updatePartialEmployeeByID(Long id, Map<String, Object> partialEmployee) {
        // throws exception if employee not found
        isEmployeeIDPresent(id);
//            get the employee, check already done
        EmployeeEntity toPatch = employeeRepository.findById(id).get();
//            Using Reflection: Actions performed by a program to inspect or modify its structure or
//            behaviour at runtime
        partialEmployee.forEach((fieldName, fieldValue) -> {
//              find the corresponding field in employee entity
            Field fieldToBeUpdated = ReflectionUtils.findField(EmployeeEntity.class, fieldName);
//                the field in EmployeeEntity is set to private, so make it accessible
            fieldToBeUpdated.setAccessible(true);
//            make the value compatible before updating
            Object convertedValue = objectMapper.convertValue(fieldValue, fieldToBeUpdated.getType());
//                2nd argument denotes the target where we want to make the changes
            ReflectionUtils.setField(fieldToBeUpdated, toPatch, convertedValue);
        });
//        some issues here with Date of Joining field as its passed as a String from the controller
//        because of using a Map<String,Object> as request type which creates problems when mapping back
//        we need to set the types of the patch parameters correctly before updating the fields using Object
//        Mapper to resolve these issues
//        update the record,partially
        return modelMapper.map(employeeRepository.save(toPatch), EmployeeDTO.class);
    }

    //    make code DRY compliant
    private void isEmployeeIDPresent(Long id) {
//        exception can be thrown here if id does not exist and all calling methods don't need to have a separate
//        check
        if (!employeeRepository.existsById(id))
            throw new ResourceNotFoundException("Employee not found with id: " + id);
    }
}
