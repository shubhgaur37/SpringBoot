package com.module2.shubh.SpringBootWebTutorial.service;

import com.module2.shubh.SpringBootWebTutorial.dto.DepartmentDTO;
import com.module2.shubh.SpringBootWebTutorial.entities.DepartmentEntity;
import com.module2.shubh.SpringBootWebTutorial.exceptions.ResourceNotFoundException;
import com.module2.shubh.SpringBootWebTutorial.repositories.DepartmentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public DepartmentService(DepartmentRepository departmentRepository, ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }


    public List<DepartmentDTO> getAllDepartments() {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        return departments.stream()
                .map(departmentEntity -> modelMapper.map(departmentEntity, DepartmentDTO.class))
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        departmentExistsById(id);
        return modelMapper.map(departmentRepository.findById(id).get(), DepartmentDTO.class);
    }

    public DepartmentDTO createDepartment(DepartmentDTO inputDepartment) {
        DepartmentEntity toSave = modelMapper.map(inputDepartment, DepartmentEntity.class);
        return modelMapper.map(departmentRepository.save(toSave), DepartmentDTO.class);
    }


    public DepartmentDTO updateDepartmentById(Long id, DepartmentDTO updateDepartment) {
        departmentExistsById(id);

        updateDepartment.setId(id);
        return modelMapper.map(departmentRepository.save(modelMapper.map(updateDepartment, DepartmentEntity.class)), DepartmentDTO.class);
    }

    public DepartmentDTO updatePartialDepartmentById(Long id, Map<String, Object> updateDepartmentPartial) {
        departmentExistsById(id);

        DepartmentEntity toPatch = departmentRepository.findById(id).get();

        for (Map.Entry<String, Object> entry : updateDepartmentPartial.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            // find field in entity
            Field fieldToBeUpdated = ReflectionUtils.findField(DepartmentEntity.class, fieldName);
            fieldToBeUpdated.setAccessible(true);
            ReflectionUtils.setField(fieldToBeUpdated, toPatch, fieldValue);
        }
        return modelMapper.map(departmentRepository.save(toPatch), DepartmentDTO.class);

    }

    public boolean deleteDepartmentById(Long id) {
        departmentExistsById(id);
        departmentRepository.deleteById(id);
        return true;
    }

    private void departmentExistsById(Long id) {
        if (!departmentRepository.existsById(id))
            throw new ResourceNotFoundException("Department with id: " + id + " not found");
    }


}
