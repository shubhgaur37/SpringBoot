package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final AdmissionService admissionService;
    private final ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(StudentService.class);

    /**
     * Returns Optional to allow Controller to handle 404 responses.
     */
    public Optional<StudentResponseDTO> getStudentById(Long id) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch Student ID: {}", id);

        return studentRepository.findById(id)
                .map(student -> {
                    StudentResponseDTO responseDTO = modelMapper.map(student, StudentResponseDTO.class);

                    // Payload State Tracking (TRACE): Safe to log the flat DTO payload
                    logger.trace("Retrieved Student details for ID {}: {}", id, responseDTO);

                    return responseDTO;
                });
    }


    @Transactional
    public StudentResponseDTO createStudent(StudentRequestDTO studentRequest) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to create a new student with Name: {}", studentRequest.getName());

        // Map Request DTO to Entity
        Student studentEntity = modelMapper.map(studentRequest, Student.class);

        // Use existing admission logic
        AdmissionRecord studentAdmissionRecord = admissionService.initialiseAdmissionRecord(studentEntity);

        // Set the link on the owning side
        studentEntity.setAdmissionRecord(studentAdmissionRecord);

        // Save and map to the specific Response DTO
        Student savedStudent = studentRepository.save(studentEntity);

        // Milestone Audit (INFO): Essential audit trail for new student records
        logger.info("Successfully created student record. Assigned ID: {}, Admission Record ID: {}",
                savedStudent.getId(), studentAdmissionRecord.getId());

        StudentResponseDTO responseDTO = modelMapper.map(savedStudent, StudentResponseDTO.class);

        // Payload State Tracking (TRACE): Safe to log the flat DTO payload
        logger.trace("Created Student full response details: {}", responseDTO);

        return responseDTO;
    }


    public List<StudentResponseDTO> getAllStudents() {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch all students");

        List<StudentResponseDTO> students = studentRepository.findAll().stream()
                .map(student -> modelMapper.map(student, StudentResponseDTO.class))
                .toList();

        // Operational Tracking (INFO): Records total count retrieved for system health monitoring
        logger.info("Successfully retrieved list of all students. Total count: {}", students.size());

        // Payload State Tracking (TRACE): Detailed item trace for debugging
        logger.trace("Full student list payload: {}", students);

        return students;
    }

    @Transactional
    public void deleteStudent(Long id) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to delete student with ID: {}", id);

        studentRepository.deleteById(id);

        // Milestone Audit (INFO): Permanent data modification tracking
        logger.info("Successfully requested database deletion for Student ID: {}", id);
    }

}